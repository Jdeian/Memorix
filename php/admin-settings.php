<?php
include "../includes/header.php"; 
include "../includes/database.php";

$super_admins = $conn->query("SELECT COUNT(*) as count FROM admin WHERE role = 'Super Admin'")->fetch_assoc()['count'];
$moderator_admins = $conn->query("SELECT COUNT(*) as count FROM admin WHERE role = 'Moderator'")->fetch_assoc()['count'];
$support_admins = $conn->query("SELECT COUNT(*) as count FROM admin WHERE role = 'Support'")->fetch_assoc()['count'];
$total_admins = $super_admins + $moderator_admins + $support_admins;

$search_term = isset($_GET['search']) ? $_GET['search'] : '';

$admins_query = "SELECT admin_id, name, email, number, role, short_description, address FROM admin";

if (!empty($search_term)) {
    $search_term = $conn->real_escape_string($search_term); 
    $admins_query .= "
    WHERE name LIKE '%$search_term%' 
    OR email LIKE '%$search_term%' 
    OR CONCAT('0', number) LIKE '%$search_term%' 
    OR role LIKE '%$search_term%'";
}

$admins_result = $conn->query($admins_query);

if (!$admins_result) {
    die("Query failed: " . $conn->error);
}

$conn->close();

include "../includes/edit_admin.php";
include "../includes/add_admin.php";
include "../includes/delete_admin.php";
?>

<head>
    <link rel="stylesheet" href="../css/admin-settings.css">
</head>
<body>
    <div id="loadingIndicator">
        <div class="spinner"></div>
    </div>

    <div class="admin-content">

    <div class="header">
        <p>Admin Settings</p>
        <?php if (isset($_SESSION['admin_id']) && ($_SESSION['admin_role'] === 'Moderator' || $_SESSION['admin_role'] === 'Super Admin')): ?>
            <button class="add-admin" onclick="openAddUserModal()">Add Admin</button>   
        <?php endif; ?>
        <form action="" method="get" class="search-container">
            <input type="text" name="search" placeholder="Search for..." value="<?php echo htmlspecialchars($search_term); ?>" aria-label="Search for users">
            <img src="../img/search_icon.png" alt="Search Icon" class="search-icon">
        </form>
    </div>

        <div class="admin-stats">
            <div class="stat-box">
                <h4>Super Admins</h4>
                <p id="super-admin-count"><?php echo $super_admins; ?></p>
            </div>
            <div class="stat-box">
                <h4>Moderator Admins</h4>
                <p id="moderator-admin-count"><?php echo $moderator_admins; ?></p>
            </div>
            <div class="stat-box">
                <h4>Support Admins</h4>
                <p id="support-admin-count"><?php echo $support_admins; ?></p>
            </div>
            <div class="stat-box">
                <h4>Total Admins</h4>
                <p id="total-admin-count"><?php echo $total_admins; ?></p>
            </div>
        </div>

        <div class="admin-data-box">
            <h4 class="admin-data-header">All Admins</h4>
            <div class="table-container">
                <div class="table-header">
                    <div class="header-item" id="nameHeader"><img src="../img/user-icon.png" alt="Name Icon">Name</div>
                    <div class="header-item"><img src="../img/gmail-icon.png" alt="Email Icon">Gmail</div>
                    <div class="header-item"><img src="../img/phone-icon.png" alt="Phone Icon">Phone</div>
                    <div class="header-item"><img src="../img/category-icon.png" alt="Category Icon">Role</div>
                    <?php if (isset($_SESSION['admin_id']) && ($_SESSION['admin_role'] === 'Moderator' || $_SESSION['admin_role'] === 'Super Admin')): ?>
                    <div class="header-actions">
                        <div class="header-item edit-item" id="editHeader">Edit</div>
                        <div class="header-item delete-item" id="deleteHeader">Delete</div>
                    </div>
                    <?php endif; ?>
                </div>

                <div class="admin-table">
                    <?php while($row = mysqli_fetch_assoc($admins_result)): ?>
                        <div class="admin-row" id="row-<?php echo $row['admin_id']; ?>">
                            <div class="admin-item">
                                <div class="admin-data">
                                    <img src="../img/name-profile-icon.png" alt="Profile" class="profile-pic">
                                    <p class="name"><?php echo $row['name']; ?></p>
                                </div>
                            </div>
                            <div class="admin-item email"><?php echo $row['email']; ?></div>
                            <div class="admin-item phone">0<?php echo $row['number']; ?></div>
                            <div class="admin-item role"><?php echo $row['role']; ?></div>

                            <div class="admin-item actions">
                            <?php
                                if (isset($_SESSION['admin_id']) && $_SESSION['admin_role'] === 'Super Admin') {
                                    echo "<img src='../img/icon_edit.png' alt='Edit' class='edit' 
                                            onclick=\"openAdminModal({$row['admin_id']}, '" . addslashes($row['name']) . "', '" . addslashes($row['email']) . "', '" . addslashes($row['short_description']) . "', '" . addslashes($row['number']) . "', '" . addslashes($row['role']) . "', '" . addslashes($row['address']) . "')\">";
                                    echo "<img src='../img/icon_delete.png' alt='Delete' class='delete' 
                                            onclick=\"openDeleteModal({$row['admin_id']})\">";
                                }
                            ?>
                            </div>
                        </div>
                    <?php endwhile; ?>
                </div>
            </div>
        </div>
    </div>
</body>
