<?php
include "../includes/header.php";
include "../includes/database.php";
include "../includes/proof_of_payment.php";

$year = isset($_GET['year']) ? intval($_GET['year']) : date("Y");
$currentYear = date("Y");

$free_users = 0;
$basic_users = 0;
$premium_users = 0;

if ($year == $currentYear) {
    $query_free_users = "
        SELECT COUNT(DISTINCT id) AS free_users 
        FROM users 
        WHERE category = 'Free' AND YEAR(created_at) IN ('$currentYear', '" . ($currentYear - 1) . "')";
    $query_basic_users = "
        SELECT COUNT(DISTINCT id) AS basic_users 
        FROM users 
        WHERE category = 'Basic' AND YEAR(created_at) IN ('$currentYear', '" . ($currentYear - 1) . "')";
    $query_premium_users = "
        SELECT COUNT(DISTINCT id) AS premium_users 
        FROM users 
        WHERE category = 'Premium' AND YEAR(created_at) IN ('$currentYear', '" . ($currentYear - 1) . "')";
} else {
    $query_free_users = "SELECT COUNT(DISTINCT id) AS free_users FROM users WHERE category = 'Free' AND YEAR(created_at) = '$year'";
    $query_basic_users = "SELECT COUNT(DISTINCT id) AS basic_users FROM users WHERE category = 'Basic' AND YEAR(created_at) = '$year'";
    $query_premium_users = "SELECT COUNT(DISTINCT id) AS premium_users FROM users WHERE category = 'Premium' AND YEAR(created_at) = '$year'";
}

$free_users_result = mysqli_query($conn, $query_free_users);
$basic_users_result = mysqli_query($conn, $query_basic_users);
$premium_users_result = mysqli_query($conn, $query_premium_users);

$free_users = mysqli_fetch_assoc($free_users_result)['free_users'];
$basic_users = mysqli_fetch_assoc($basic_users_result)['basic_users'];
$premium_users = mysqli_fetch_assoc($premium_users_result)['premium_users'];

$query_revenue = "
    SELECT category, SUM(amount) AS total_revenue
    FROM subscriptions 
    WHERE YEAR(created_at) = '$year'
    GROUP BY category";
    
$revenue_result = mysqli_query($conn, $query_revenue);

$basic_revenue = 0;
$premium_revenue = 0;

while ($row = mysqli_fetch_assoc($revenue_result)) {
    if ($row['category'] === 'Basic') {
        $basic_revenue = $row['total_revenue'];
    } elseif ($row['category'] === 'Premium') {
        $premium_revenue = $row['total_revenue'];
    }
}

$total_revenue = $basic_revenue + $premium_revenue;

$data = [
    'free_users' => $free_users,
    'basic_users' => $basic_users,
    'premium_users' => $premium_users,
    'average_revenue' => $total_revenue, 
];

$usersDataJson = json_encode($data);

$pending_query = "
    SELECT sp.user_id, sp.id, sp.amount, sp.created_at, u.fullname, u.category, u.email, 
           sp.pending_status AS status, sp.proof_payment
    FROM subcriptions_pending sp
    INNER JOIN users u ON sp.user_id = u.id
    WHERE sp.pending_status = 'Pending'
";

$pending_result = mysqli_query($conn, $pending_query);
?>

<head>
    <link rel="stylesheet" href="../css/dashboard.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="dashboard-content">
        <h1 class="header">Analytics</h1>
        <div class="analytic-stats">
            <div class="stat-box">
                <h4>Free Users</h4>
                <div class="count-container">
                    <p id="free-user-count"><?php echo $free_users; ?></p>
                    <div class="analytics">
                        <p class="analytics-count">21.5%</p>
                        <img src="../img/analytics-high-icon.png" alt="" class="analytics-img">
                    </div>
                </div>
            </div>
            <div class="stat-box">
                <h4>Basic Users</h4>
                <div class="count-container">
                    <p id="basic-user-count"><?php echo $basic_users; ?></p>
                    <div class="analytics">
                        <p class="analytics-count">21.5%</p>
                        <img src="../img/analytics-high-icon.png" alt="" class="analytics-img">
                    </div>
                </div>
            </div>
            <div class="stat-box">
                <h4>Premium Users</h4>
                <div class="count-container">
                    <p id="premium-user-count"><?php echo $premium_users; ?></p>
                    <div class="analytics">
                        <p class="analytics-count">21.5%</p>
                        <img src="../img/analytics-high-icon.png" alt="" class="analytics-img">
                    </div>
                </div>
            </div>
            <div class="stat-box">
                <h4>Average Revenue</h4>
                <div class="count-container">
                    <p id="average-revenue"><?php echo '₱' . number_format($total_revenue); ?></p>
                    <div class="analytics">
                        <p class="analytics-count">21.5%</p>
                        <img src="../img/analytics-high-icon.png" alt="" class="analytics-img">
                    </div>
                </div>
            </div>
        </div>

        <div class="charts-container">
            <div id="barChart-container">
                <h4 class="revenue-text">Revenue</h4>
                <div class="barChart-header">
                    <div class="revenue-container">
                        <p id="total-revenue"><?php echo '₱' . number_format($total_revenue); ?></p>
                        <div class="analytics">
                            <p class="analytics-count">21.5%</p>
                            <img src="../img/analytics-high-icon.png" alt="" class="analytics-img">
                        </div>
                        <div id="customLegend" class="custom-legend">
                            <div class="legend-item">
                                <span class="legend-color" style="background-color: #0E43FB;"></span>
                                <span class="legend-label">Basic Users</span>
                            </div>
                            <div class="legend-item">
                                <span class="legend-color" style="background-color: #00C2FF;"></span>
                                <span class="legend-label">Premium Users</span>
                            </div>
                        </div>
                    </div>
                    <div class="date-container">
                        <select id="year-selector" class="year-selector">
                            <?php 
                            for ($year = 2023; $year <= date("Y"); $year++) {
                                echo "<option value='$year'>$year</option>";
                            }
                            ?>
                        </select>
                    </div>
                </div>
                <canvas id="barChart"></canvas>
            </div>

            <div id="donutChart-container">
                <h2>Overall Users</h2>
                <canvas id="donutChart"></canvas>
                <div id="customLegend-piechart" class="custom-legend">
                    <div class="legend-item">
                        <span class="legend-color" style="background-color: #02EBB1;"></span>
                        <span class="legend-label">Free Users</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-color" style="background-color: #0E43FB;"></span>
                        <span class="legend-label">Basic Users</span>
                    </div>
                    <div class="legend-item">
                        <span class="legend-color" style="background-color: #00C2FF;"></span>
                        <span class="legend-label">Premium Users</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="user-data-box">
            <h4 class="user-data-header">Request Payments</h4>
            <div class="table-container">
                <div class="table-header">
                    <div id="nameHeader" class="header-item"><img src="../img/user-icon.png" alt="Name Icon">Name</div>
                    <div class="header-item"><img src="../img/birthdate-icon.png" alt="Date Icon">Requested On</div>
                    <div id="emailHeader" class="header-item"><img src="../img/gmail-icon.png" alt="Email Icon">Gmail</div>
                    <div id="totalHeader" class="header-item"><img src="../img/category-icon.png" alt="Total Icon">Total</div>
                    <div id="paymentHeader" class="header-item"><img src="../img/proof_payment_icon.png" alt="Proof Icon">Payment Proof</div>
                    <div id="statusHeader" class="header-item"><img src="../img/status-icon.png" alt="Status Icon">Status</div>
                    <?php if (isset($_SESSION['admin_id']) && ($_SESSION['admin_role'] === 'Moderator' || $_SESSION['admin_role'] === 'Super Admin')): ?>
                    <div class="header-actions">
                    <div class="header-item" id="acceptHeader">Accept</div>
                    <div class="header-item" id="declineHeader">Decline</div> 
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
                            <p id="accept-icon" class="action-icon" onclick="updateStatus(<?= $row['user_id'] ?>, <?= $row['id'] ?>, 'Accepted')">Accept</p>
                            </div>
                            <div class="user-item">
                                <p id="decline-icon" class="action-icon" onclick="updateStatus(<?= $row['user_id'] ?>, <?= $row['id'] ?>, 'Declined')">Decline</p>
                            </div>
                            <?php endif; ?>
                        </div>
                    <?php endwhile; ?>
                </div>
            </div>
        </div>
    </div>

    <script src="../js/dashboard.js"></script>

    <script>
    function updateStatus(userId, pendingId, action) {
            if (confirm(`Are you sure you want to ${action} this subscription?`)) {
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
                xhr.send(`user_id=${userId}&pending_id=${pendingId}&action=${action}`);
            }
        }
        
        function deleteSubscription(pendingId) {
            if (confirm("Are you sure you want to delete this subscription?")) {
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
