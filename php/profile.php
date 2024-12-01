<?php
include "../includes/header.php";
include "../includes/database.php";

$admin_id = $_SESSION['admin_id'];
$name = $email = $short_description = $number = $role = $address = "";
$profile = "../img/name-profile-icon.png";

$sql = "SELECT * FROM admin WHERE admin_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $admin_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $admin = $result->fetch_assoc();
    $name = htmlspecialchars($admin['name']);
    $email = htmlspecialchars($admin['email']);
    $short_description = htmlspecialchars($admin['short_description']);
    $number = htmlspecialchars($admin['number']);
    $role = htmlspecialchars($admin['role']);
    $address = htmlspecialchars($admin['address']);

    if (!empty($admin['profile'])) {
        $profile = htmlspecialchars($admin['profile']); 
    }
}

$stmt->close();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = htmlspecialchars($_POST['admin-name']);
    $short_description = htmlspecialchars($_POST['admin-description']);
    $number = htmlspecialchars($_POST['admin-number']);
    $address = htmlspecialchars($_POST['admin-address']);
    $profile_image_path = ""; 

    if (isset($_FILES['admin-image-upload']) && $_FILES['admin-image-upload']['error'] == UPLOAD_ERR_OK) {
        $allowed_types = ['image/png', 'image/jpeg', 'image/gif', 'image/svg+xml'];
        $file_type = $_FILES['admin-image-upload']['type'];
        
        if (in_array($file_type, $allowed_types)) {
            $upload_dir = '../profile-photos/';
            $file_name = $name . '.' . pathinfo($_FILES['admin-image-upload']['name'], PATHINFO_EXTENSION); 
            $profile_image_path = $upload_dir . $file_name; 
            
            if (move_uploaded_file($_FILES['admin-image-upload']['tmp_name'], $profile_image_path)) {
               
            } else {
                echo "<script>alert('Error uploading the image.');</script>";
            }
        } else {
            echo "<script>alert('Invalid file type. Only PNG, JPEG, GIF, and SVG are allowed.')</script>";
        }
    }

    if ($profile_image_path) {
      $update_sql = "UPDATE admin SET name=?, short_description=?, number=?, address=?, profile=? WHERE admin_id=?";
      $update_stmt = $conn->prepare($update_sql);
      $update_stmt->bind_param("ssissi", $name, $short_description, $number, $address, $profile_image_path, $admin_id); 
      $update_stmt->execute();     
    } else {
        $update_sql = "UPDATE admin SET name=?, short_description=?, number=?, address=? WHERE admin_id=?";
        $update_stmt = $conn->prepare($update_sql);
        $update_stmt->bind_param("ssisi", $name, $short_description, $number, $address, $admin_id);
        $update_stmt->execute();
        echo "<script>console.log('Role value: " . $role . "');</script>";
    }

    $update_stmt->close();
}

$conn->close();
?>
<header>
<link rel="stylesheet" href="../css/profile.css">
</header> 
<body>
<div id="loadingIndicator">
        <div class="spinner"></div>
</div>
<div class="profile-edit-container">
  <h2>Personal Information</h2>
  <form method="POST" enctype="multipart/form-data" action="profile.php">
    <div class="personal-information-container">

      <div class="input-container">
        <label for="admin-name">Full Name:</label>
        <div class="input-box">
          <div class="input-icon">
            <img src="<?php echo $profile; ?>" alt="Profile Icon" class="profile-icon">
          </div>
          <input type="text" id="admin-name" name="admin-name" value="<?php echo $name; ?>" required>
        </div>
      </div>

      <div class="input-container">
        <label for="admin-email">Email:</label>
        <input type="email" id="admin-email" name="admin-email" value="<?php echo $email; ?>" required>
      </div>

      <div class="upload-image-container">
        <label for="admin-image-upload" id="profileimgtxt">Profile Image:</label>
        <div class="image-upload-box" id="image-upload-box">
          <img id="profile-preview" src="<?php echo $profile; ?>" alt="Admin Profile" class="admin-profile">
          <div class="upload-instructions">
            <img src="../img/upload-icon.png" alt="Upload Icon" class="upload-icon">
            <p><span class="click-upload">Click to upload</span> or drag and drop<br> SVG, PNG, JPG, or GIF (max 800 x 400px)</p>
            <input type="file" id="admin-image-upload" name="admin-image-upload" accept=".svg, .png, .jpg, .gif" hidden>
          </div>
        </div>
      </div>

      <div class="input-container">
        <label for="admin-description">Bio:</label>
        <textarea id="admin-description" name="admin-description" rows="4"><?php echo $short_description; ?></textarea>
      </div>
    </div>

    <h2>Basic Information</h2>
    <div class="basic-information-container">
      <div class="input-container">
        <label for="admin-number">Number:</label>
        <input type="text" id="admin-number" name="admin-number" value="0<?php echo $number; ?>" required>
      </div>

      <div class="input-container">
      <label for="admin-role">Role:</label>
      <p id="admin-role"><?php echo htmlspecialchars($role); ?></p>
      </div>


      <div class="input-container">
        <label for="admin-address">Address:</label>
        <input type="text" id="admin-address" name="admin-address" value="<?php echo $address; ?>" required>
      </div>
    </div>

    <div class="button-container">
      <button type="submit" class="save-btn">Save</button>
    </div>
  </form>
</div>
</body>

<script>
  document.addEventListener("DOMContentLoaded", function () {
    const imageUploadBox = document.getElementById("image-upload-box");
    const uploadInput = document.getElementById("admin-image-upload");
    const profilePreview = document.getElementById("profile-preview");

    imageUploadBox.addEventListener("click", function () {
      uploadInput.click();
    });

    uploadInput.addEventListener("change", function (event) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
          profilePreview.src = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    });

    imageUploadBox.addEventListener("dragover", function (e) {
      e.preventDefault();
      e.stopPropagation();
      imageUploadBox.classList.add("dragging");
    });

    imageUploadBox.addEventListener("dragleave", function (e) {
      e.preventDefault();
      e.stopPropagation();
      imageUploadBox.classList.remove("dragging");
    });

    imageUploadBox.addEventListener("drop", function (e) {
      e.preventDefault();
      e.stopPropagation();
      imageUploadBox.classList.remove("dragging");

      const file = e.dataTransfer.files[0];
      if (file) {
        uploadInput.files = e.dataTransfer.files;
        const reader = new FileReader();
        reader.onload = function (e) {
          profilePreview.src = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    });
  });
</script>
