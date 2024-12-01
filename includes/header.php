<?php
session_start();

if (!isset($_SESSION['admin_logged_in'])) {
    header("Location: ../php/login.php"); 
    exit();
}

include 'database.php';

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$admin_id = $_SESSION['admin_id'];

$query = "SELECT profile, name FROM admin WHERE admin_id = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("i", $admin_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $profileImage = $row['profile'] ?: '../img/name-profile-icon.png'; 
    $userName = htmlspecialchars($row['name']); 
} else {
    $profileImage = '../img/default-profile.png'; 
    $userName = "User"; 
}

$stmt->close();
$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Memorix Admin</title>
  <link href="https://cdn.jsdelivr.net/npm/tailwindcss@3.3.2/dist/tailwind.min.css" rel="stylesheet">
  <link rel="icon" href="../img/memorix-logo.png">
  <link rel="stylesheet" href="../css/header.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Work+Sans:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=ADLaM+Display&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=ADLaM+Display&family=Akatab:wght@400;500;600;700;800;900&family=Work+Sans:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
</head>
<body>
  <div class="sidebar">
    <div class="logo">
      <img src="../img/memorix-logo.png" alt="Memorix Logo" class="memorix_logo">
    </div>
    <div class="menu-item">
      <a href="../php/dashboard.php" class="menu-header" id="dashboard">
        <div class="bluegreen"></div>
        <img src="../img/dashboard-icon.png" alt="" class="dashboard_icon">
        <p>Dashboard</p>
        <img src="../img/arrow_right.png" alt="" class="arrow_bottom">
      </a>
    </div>

    <div class="menu-item">
      <a href="../php/history.php" class="menu-header" id="user">
        <div class="bluegreen"></div>
        <img src="../img/history-icon.png" alt="" class="user_icon">
        <p id="payment-p">Payment History</p>
        <img src="../img/arrow_right.png" alt="" class="arrow_bottom">
      </a>
    </div>

    <div class="menu-item">
      <a href="../php/user.php" class="menu-header" id="user">
        <div class="bluegreen"></div>
        <img src="../img/Users Icon.png" alt="" class="user_icon">
        <p>User</p>
        <img src="../img/arrow_right.png" alt="" class="arrow_bottom">
      </a>
    </div>
    <div class="menu-item" id="all-pages-submenu" style="display: none;">
    </div>

    <div class="line"></div>

    <div class="menu-item">
      <a href="../php/profile.php" class="profile-header" id="profile">
        <div class="bluegreen"></div>
        <img src="<?php echo htmlspecialchars($profileImage); ?>" alt="Profile Image" class="profile-image">
        <div class="profile_container">
          <p class="profile-name"><?php echo $userName; ?></p>
          <p style="font-size: 12px; color: #AEB9E1;">Account settings</p>
        </div>
        <img src="../img/arrow_right.png" alt="" class="profile_arrow_bottom">
        </a>
    
    </div>

    <div class="menu-item">
      <a href="../php/admin-settings.php" class="menu-header" id="admin-settings">
        <div class="bluegreen"></div>
        <img src="../img/star_icon.png" alt="" class="admin_icon">
        <p>Admin Settings</p>
        <img src="../img/arrow_right.png" alt="" class="arrow_bottom">
      </a>
    </div>
    
    <div class="menu-item">
      <a href="../php/logout.php" class="menu-header" id="admin-logout">
        <div class="bluegreen"></div>
        <img src="../img/logout-icon.png" alt="" class="admin_icon">
        <p>Log out</p>
        <img src="../img/arrow_right.png" alt="" class="arrow_bottom">
      </div>
      </a>
  </div>
  
  <script>
    document.addEventListener("DOMContentLoaded", function() {
    const currentUrl = window.location.pathname.split('/').pop(); 
    const menuItems = document.querySelectorAll('.menu-header');

    menuItems.forEach(item => {
    const link = item.getAttribute('href').split('/').pop();
    if (currentUrl === link) {
        const bluegreenDiv = item.querySelector('.bluegreen');
        if (bluegreenDiv) {
            bluegreenDiv.style.width = '7px';
        }

        const paragraph = item.querySelector('p');
        if (paragraph) {
            paragraph.style.background = 'linear-gradient(90deg, #87DDEE 0%, #B0EDA7 100%)';
            paragraph.style.webkitBackgroundClip = 'text';
            paragraph.style.webkitTextFillColor = 'transparent';
        }

        item.style.backgroundColor = '#0A1330';
    }
});

const profileHeader = document.getElementById('profile');
        if (currentUrl === '../php/profile.php') {
            const bluegreenDiv = profileHeader.querySelector('.bluegreen');
            if (bluegreenDiv) {
                bluegreenDiv.style.width = '7px';
            }

            const paragraph = profileHeader.querySelector('.profile-name');
            if (paragraph) {
                paragraph.style.background = 'linear-gradient(90deg, #87DDEE 0%, #B0EDA7 100%)';
                paragraph.style.webkitBackgroundClip = 'text';
                paragraph.style.webkitTextFillColor = 'transparent';
            }

            profileHeader.style.backgroundColor = '#0A1330';
        }

});

</script>
</body>
</html>
