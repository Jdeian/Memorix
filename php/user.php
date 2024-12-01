<?php
include "../includes/database.php";

$users_per_page = 10;
$page = isset($_GET['page']) && is_numeric($_GET['page']) ? intval($_GET['page']) : 1;
$search_term = isset($_GET['search']) ? mysqli_real_escape_string($conn, $_GET['search']) : '';
$offset = ($page - 1) * $users_per_page;

$update_user_status_query = "
    UPDATE users 
    SET status = CASE 
        WHEN last_login < NOW() - INTERVAL 30 DAY THEN 'Inactive' 
        ELSE 'Active' 
    END
";
mysqli_query($conn, $update_user_status_query);

if (strtolower($search_term) == 'active' || strtolower($search_term) == 'inactive') {
    $search_query = "LOWER(status) = LOWER('$search_term')";
} else {
    $search_query = "
        LOWER(fullname) LIKE LOWER('%$search_term%') 
        OR LOWER(email) LIKE LOWER('%$search_term%') 
        OR LOWER(category) LIKE LOWER('%$search_term%') 
        OR LOWER(status) LIKE LOWER('%$search_term%') 
        OR DATE_FORMAT(created_at, '%Y-%m-%d') LIKE '%$search_term%'
    ";
}

$query_total_users = "SELECT COUNT(*) AS total_users FROM users WHERE $search_query";
$total_users_result = mysqli_query($conn, $query_total_users);
$total_users = mysqli_fetch_assoc($total_users_result)['total_users'];

$query_free_users = "SELECT COUNT(*) AS free_users FROM users WHERE category = 'Free'";
$query_basic_users = "SELECT COUNT(*) AS basic_users FROM users WHERE category = 'Basic'";
$query_premium_users = "SELECT COUNT(*) AS premium_users FROM users WHERE category = 'Premium'";
$query_inactive_users = "SELECT COUNT(*) AS inactive_users FROM users WHERE status = 'Inactive'";

$free_users_result = mysqli_query($conn, $query_free_users);
$basic_users_result = mysqli_query($conn, $query_basic_users);
$premium_users_result = mysqli_query($conn, $query_premium_users);
$inactive_users_result = mysqli_query($conn, $query_inactive_users);

$free_users = mysqli_fetch_assoc($free_users_result)['free_users'];
$basic_users = mysqli_fetch_assoc($basic_users_result)['basic_users'];
$premium_users = mysqli_fetch_assoc($premium_users_result)['premium_users'];
$inactive_users = mysqli_fetch_assoc($inactive_users_result)['inactive_users'];

$query_users = "
    SELECT id, fullname, email, category, status, created_at 
    FROM users 
    WHERE $search_query
    LIMIT $users_per_page OFFSET $offset
";
$users_result = mysqli_query($conn, $query_users);

$total_pages = ceil($total_users / $users_per_page);

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_id = $_POST['user_id'];
    $fullname = $_POST['fullname'];
    $birthdate = $_POST['birthdate'];
    $email = $_POST['email'];
    $category = $_POST['category'];
    $status = isset($_POST['status']) && $_POST['status'] === 'Inactive' ? 'Inactive' : 'Active';
    $last_login = $status === 'Inactive' ? date('Y-m-d H:i:s', strtotime('-40 days')) : date('Y-m-d H:i:s');

    $update_query = "UPDATE users SET fullname=?, birthdate=?, email=?, category=?, status=?, last_login=? WHERE id=?";
    $stmt = $conn->prepare($update_query);
    $stmt->bind_param("ssssssi", $fullname, $birthdate, $email, $category, $status, $last_login, $user_id);

    if ($stmt->execute()) {
        $_SESSION['editUserMessage'] = 'User updated successfully';
        header("Location: ../php/user.php?page=$page&search=$search_term");
        exit();
    } else {
        echo "Error updating user: " . $stmt->error;
    }

    $stmt->close();
}
$conn->close();

include "../includes/header.php"; 
include "../includes/edit_user.php";
include "../includes/delete_user.php";
include "../includes/add_user.php";
?>

<head>
    <link rel="stylesheet" href="../css/user.css">
</head>
<body>
    <div id="loadingIndicator">
        <div class="spinner"></div>
    </div>
    <div class="content">
        <div class="header">
            <p>Users</p>
            <button class="add-user" onclick="openAddUserModal()">Add User</button>
            <form action="" method="get" class="search-container">
                <input type="text" name="search" placeholder="Search for..." value="<?php echo htmlspecialchars($search_term); ?>" aria-label="Search for users">
                <img src="../img/search_icon.png" alt="Search Icon" class="search-icon">
            </form>
        </div>

        <div class="user-stats">
            <div class="stat-box">
                <h4>Total Users</h4>
                <p id="total-numbers-count"><?php echo $total_users; ?></p>
            </div>
            <div class="stat-box">
                <h4>Free Users</h4>
                <p id="free-user-count"><?php echo $free_users; ?></p>
            </div>
            <div class="stat-box">
                <h4>Basic Users</h4>
                <p id="basic-user-count"><?php echo $basic_users; ?></p>
            </div>
            <div class="stat-box">
                <h4>Premium Users</h4>
                <p id="premium-user-count"><?php echo $premium_users; ?></p>
            </div>
        </div>

        <div class="user-data-box">
            <h4 class="user-data-header">All Users</h4>
            <div class="table-container">
                <div class="table-header">
                    <div class="header-item"><img src="../img/user-icon.png" alt="Name Icon">Name</div>
                    <div class="header-item"><img src="../img/birthdate-icon.png" alt="Date Icon">Date Joined</div>
                    <div class="header-item"><img src="../img/gmail-icon.png" alt="Email Icon">Gmail</div>
                    <div class="header-item"><img src="../img/category-icon.png" alt="Category Icon">Category</div>
                    <div class="header-item"><img src="../img/status-icon.png" alt="Status Icon">Status</div>
                    <?php if (isset($_SESSION['admin_id']) && ($_SESSION['admin_role'] === 'Moderator' || $_SESSION['admin_role'] === 'Super Admin')): ?>
                    <div class="header-actions">
                        <div class="header-item edit-item" id="editHeader">Edit</div>
                        <div class="header-item delete-item" id="deleteHeader">Delete</div>
                    </div>
                    <?php endif; ?>
                </div>  

                <div class="user-table">
                    <?php while($row = mysqli_fetch_assoc($users_result)): ?>
                        <div class="user-row" id="row-<?php echo $row['id']; ?>">
                            <div class="user-item">
                                <div class="user-data">
                                    <img src="../img/name-profile-icon.png" alt="Profile" class="profile-pic">
                                    <p class="name"><?php echo $row['fullname']; ?></p>
                                </div>
                            </div>
                            <div class="user-item birthdate"><?php echo $row['created_at']; ?></div>
                            <div class="user-item email"><?php echo $row['email']; ?></div>
                            <div class="user-item category"><?php echo $row['category']; ?></div>
                            <div class="user-item <?php echo $row['status'] == 'Inactive' ? 'status-inactive' : 'status'; ?>">
                                <?php echo $row['status']; ?>
                            </div>
                            <?php if (isset($_SESSION['admin_id']) && ($_SESSION['admin_role'] === 'Moderator' || $_SESSION['admin_role'] === 'Super Admin')): ?>
                                <div class="user-item">
                                    <img src="../img/icon_edit.png" alt="Edit" class="edit" 
                                        onclick="openModal(<?php echo $row['id']; ?>, '<?php echo addslashes($row['fullname']); ?>', '<?php echo $row['created_at']; ?>', '<?php echo addslashes($row['email']); ?>', '<?php echo addslashes($row['category']); ?>', '<?php echo addslashes($row['status']); ?>')">
                                    <img src="../img/icon_delete.png" alt="Delete" class="delete" 
                                        onclick="openDeleteModal(<?php echo $row['id']; ?>)">
                                </div>
                            <?php endif; ?>
                        </div>
                    <?php endwhile; ?>
                </div>
            </div>

            <div class="pagination">
                    <div class="page-buttons">
                        <?php if ($page > 1): ?>
                            <a href="?page=<?php echo $page - 1; ?>&search=<?php echo urlencode($search_term); ?>">Previous</a>
                        <?php endif; ?>

                        <?php for ($i = 1; $i <= $total_pages; $i++): ?>
                            <a href="?page=<?php echo $i; ?>&search=<?php echo urlencode($search_term); ?>" class="<?php echo $i === $page ? 'active' : ''; ?>">
                                <?php echo $i; ?>
                            </a>
                        <?php endfor; ?>

                        <?php if ($page < $total_pages): ?>
                            <a href="?page=<?php echo $page + 1; ?>&search=<?php echo urlencode($search_term); ?>">Next</a>
                        <?php endif; ?>
                    </div>
            </div>
        </div>
    </div>
</body>
