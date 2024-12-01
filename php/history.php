<?php
include "../includes/header.php";
include "../includes/database.php";
include "../includes/proof_of_payment.php";

$users_per_page = 10;
$page = isset($_GET['page']) && is_numeric($_GET['page']) ? intval($_GET['page']) : 1;
$search_term = isset($_GET['search']) ? trim(mysqli_real_escape_string($conn, $_GET['search'])) : '';
$offset = ($page - 1) * $users_per_page;

$search_term_lower = strtolower($search_term); 

$query_total_users = "
    SELECT COUNT(DISTINCT sp.user_id) AS total_users 
    FROM subcriptions_history sp
    INNER JOIN users u ON sp.user_id = u.id
    WHERE (
        LOWER(u.fullname) LIKE '%$search_term_lower%' OR
        LOWER(u.email) LIKE '%$search_term_lower%' OR
        sp.created_at LIKE '%$search_term_lower%' OR
        LOWER(sp.pending_status) LIKE '%$search_term_lower%'
    )
";

if (!empty($status_filter)) {
    $query_total_users .= " AND sp.pending_status = '$status_filter'";
}

$total_users_result = mysqli_query($conn, $query_total_users);
$total_users = mysqli_fetch_assoc($total_users_result)['total_users'];
$total_pages = ceil($total_users / $users_per_page);

$pending_query = "
    SELECT sp.user_id, sp.id, sp.amount, sp.created_at, u.fullname, u.category, u.email, 
           sp.pending_status AS status, sp.proof_payment
    FROM subcriptions_history sp
    INNER JOIN users u ON sp.user_id = u.id
    WHERE (
        LOWER(u.fullname) LIKE '%$search_term_lower%' OR
        LOWER(u.email) LIKE '%$search_term_lower%' OR
        sp.created_at LIKE '%$search_term_lower%' OR
        LOWER(sp.pending_status) LIKE '%$search_term_lower%'
    )
";

if (!empty($status_filter)) {
    $pending_query .= " AND sp.pending_status = '$status_filter'";
}

$pending_query .= " 
    LIMIT $users_per_page OFFSET $offset
";

$pending_result = mysqli_query($conn, $pending_query);
?>

<head>
    <link rel="stylesheet" href="../css/history.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="dashboard-content">

           <form method="get" action="" class="search-container">
                <input type="text"  name="search" placeholder="Search for..." value="<?php echo htmlspecialchars($search_term); ?>">
                <img src="../img/search_icon.png" alt="Search Icon" class="search-icon">
            </form>

        <div class="user-data-box">
            <h4 class="user-data-header">Payment History</h4>
            <div class="table-container">
                <div class="table-header">
                    <div id="nameHeader" class="header-item"><img src="../img/user-icon.png" alt="Name Icon">Name</div>
                    <div id="dateHeader" class="header-item"><img src="../img/birthdate-icon.png" alt="Date Icon">Requested On</div>
                    <div id="emailHeader" class="header-item"><img src="../img/gmail-icon.png" alt="Email Icon">Gmail</div>
                    <div id="totalHeader" class="header-item"><img src="../img/category-icon.png" alt="Total Icon">Total</div>
                    <div id="paymentHeader" class="header-item"><img src="../img/proof_payment_icon.png" alt="Proof Icon">Payment Proof</div>
                    <div id="statusHeader" class="header-item"><img src="../img/status-icon.png" alt="Status Icon">Status</div>
                    <?php if (isset($_SESSION['admin_id']) && ($_SESSION['admin_role'] === 'Moderator' || $_SESSION['admin_role'] === 'Super Admin')): ?>
                    <div class="header-actions">
                        <div class="header-item" id="deleteHeader">Delete</div>
                    </div>
                    <?php endif; ?>
                </div>

                <div class="user-table">
                    <?php while ($row = mysqli_fetch_assoc($pending_result)): ?>
                        <div class="user-row" data-user-id="<?= $row['user_id'] ?>" id="user-row-<?= $row['id'] ?>">
                            <div class="user-item">
                                <div class="user-data">
                                    <img src="../img/name-profile-icon.png" alt="Profile" class="profile-pic">
                                    <div class="user-item name"><?= htmlspecialchars($row['fullname']) ?></div>
                                </div>
                            </div>
                            <div class="user-item date"><?= htmlspecialchars(date("Y-m-d", strtotime($row['created_at']))) ?></div>
                            <div class="user-item email"><?= htmlspecialchars($row['email']) ?></div>
                            <div class="user-item amount"><?= '₱' . htmlspecialchars(number_format($row['amount'])) ?></div>
                            <div class="user-item proof-payment" data-proof-path="<?= htmlspecialchars($row['proof_payment']) ?>">
                            <a href="javascript:void(0);" onclick="showProofOfPayment(this)"><?= htmlspecialchars($row['proof_payment']) ?></a>
                            </div>
                            <div class="user-item <?php 
                                if ($row['status'] == 'Declined') {
                                    echo 'status-decline';
                                } elseif ($row['status'] == 'Accepted') {
                                    echo 'status-accept';
                                } else {
                                    echo 'status';
                                }
                            ?>">
                                <?php echo $row['status']; ?>
                            </div>
                            <?php if (isset($_SESSION['admin_id']) && ($_SESSION['admin_role'] === 'Moderator' || $_SESSION['admin_role'] === 'Super Admin')): ?>
                            <div class="user-item">
                                <img src="../img/icon_delete.png" id="delete-icon" class="action-icon" onclick="deleteSubscription(<?= $row['id'] ?>)">
                            </div>
                            <?php endif; ?>
                        </div>
                    <?php endwhile; ?>
                </div>
            </div>

            <div class="pagination">
                <div class="page-buttons">
                    <?php if ($page > 1): ?>
                        <a href="?page=<?php echo $page - 1; ?>&search=<?php echo urlencode($search_term); ?>&status=<?php echo urlencode($status_filter); ?>">Previous</a>
                    <?php endif; ?>

                    <?php for ($i = 1; $i <= $total_pages; $i++): ?>
                        <a href="?page=<?php echo $i; ?>&search=<?php echo urlencode($search_term); ?>&status=<?php echo urlencode($status_filter); ?>" class="<?php echo $i === $page ? 'active' : ''; ?>">
                            <?php echo $i; ?>
                        </a>
                    <?php endfor; ?>

                    <?php if ($page < $total_pages): ?>
                        <a href="?page=<?php echo $page + 1; ?>&search=<?php echo urlencode($search_term); ?>&status=<?php echo urlencode($status_filter); ?>">Next</a>
                    <?php endif; ?>
                </div>
            </div>
        </div>
    </div>

    <script src="../js/dashboard.js"></script>

    <script>
        function deleteSubscription(pendingId) {
            if (confirm("Are you sure you want to delete this payment history?")) {
                const xhr = new XMLHttpRequest();
                xhr.open("POST", "../includes/handle_subscription_action.php", true);
                xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                xhr.onload = function() {
                    const response = JSON.parse(xhr.responseText);
                    if (response.success) {
                        alert(response.message);
                        location.reload();
                    } else {
                        alert(response.message);
                    }
                };
                xhr.send(`pending_id=${pendingId}&action=Delete`);
            }
        }   
    </script>

</body> 
</html>
