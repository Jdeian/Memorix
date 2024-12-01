<?php
session_start();
include "../includes/database.php";

$error_message = '';
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = $_POST['email'];
    $password = $_POST['password'];

    $query = "SELECT admin_id, email, password, role FROM admin WHERE email = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 1) {
        $admin = $result->fetch_assoc(); 
        if (password_verify($password, $admin['password'])) {
            $_SESSION['admin_logged_in'] = true; 
            $_SESSION['admin_id'] = $admin['admin_id']; 
            $_SESSION['admin_role'] = $admin['role'];
            header("Location: ../php/dashboard.php"); 
            exit();
        } else {
            $error_message = "Invalid email or password.";
        }
    } else {
        $error_message = "Invalid email or password.";
    }
}

$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
<style>
    body {
        margin: 0;
        height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #081028;
        font-family: 'Work Sans', sans-serif;
    }

    h2 {
        font-family: 'Montserrat', sans-serif;
        font-size: 30px;
        font-weight: 400;
        background: linear-gradient(90deg, #87DDEE 0%, #B0EDA7 100%);
        -webkit-background-clip: text; 
        -webkit-text-fill-color: transparent;
    }

    .login-container {
        display: flex; 
        width: 900px; 
        height: 600px; 
        border-radius: 10px;
        overflow: hidden; 
        border: 1px solid #343B4F; 
    }

    .login-form {
        width: 50%; 
        padding: 30px; 
        background-color: #0B1739; 
        display: flex;
        flex-direction: column;
        justify-content: center; 
        text-align: center;
    }   

    .welcome-message {
        font-size: 25px;
        color: #AEB9E1;
        font-weight: 400;
        font-family: 'Montserrat', sans-serif;
    }

    .right-image img {
        width: 100%; 
        height: 100%; 
        object-fit: cover; 
    }

    .logo {
        position: absolute;
        width: 200px; 
        margin-top: -530px;
        margin-left: -30px;
    }

    .input-container {
        position: relative;
        width: 100%;
        margin: 10px 0;
    }

    .input-container img {
        position: absolute;
        left: 10px;
        top: 40%;
        transform: translateY(-50%); 
        width: 20px; 
        height: 20px; 
    }

    .login-container input[type="email"],
    .login-container input[type="password"], 
    .login-container input[type="text"] {
        width: 85%;
        padding: 15px 10px 15px 35px; 
        border: 1px solid #343B4F; 
        border-radius: 5px;
        background-color: #0A142D; 
        color: white;
        transition: border-color 0.3s;
        font-size: 15px; 
        font-weight: 400;
        margin-bottom: 10px;
        padding-right: 40px;
    }

    .login-container input[type="submit"] {
        background-color: #02EBB1;
        border: none;
        padding: 12px;
        border-radius: 5px;
        cursor: pointer;
        transition: background-color 0.3s;
        width: 95.4%; 
        font-size: 1.3em; 
        background: linear-gradient(89.42deg, #82DBF7 0%, #B6F09C 100%);
    }

    .login-container input[type="submit"]:hover {
        transition: 0.5s ease;
        background: linear-gradient(89.42deg, #6ED7F7 0%, #9BEA8C 100%);
    }

    .forgot-password {
        margin-top: 15px;
        font-size: 0.9em;
        margin-left: 270px;
    }

    .forgot-password a {
        color: #02EBB1;
        text-decoration: none;
        transition: color 0.3s;
    }

    .forgot-password a:hover {
        color: #01b08d;
    }

    .error-message {
        display: none; 
        position: absolute;
        top: 0;
        left: 50%;
        width: cover;
        transform: translateX(-50%);
        background-color: #ff0000;
        color: #fff;
        padding: 10px;
        border-radius: 4px;
        z-index: 10;
        transition: opacity 0.5s;
        opacity: 0; 
        margin-top: 10px; 
    }

    .show-error {
        display: block;
        opacity: 1;
    }

    #togglePassword {
        position: absolute;
        right: 15px;
        transform: translateY(-50%);
        cursor: pointer;
        width: 20px;
        height: 20px;
        filter: invert(100%);
        margin-left: 380px;
        z-index: 1000;
    }

</style>
</head>
<body>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Work+Sans:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=ADLaM+Display&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=ADLaM+Display&family=Akatab:wght@400;500;600;700;800;900&family=Work+Sans:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

<div class="login-container">
    <div class="login-form">
        <img src="../img/login-logo.png" alt="Logo" class="logo">
        <div class="welcome-message">Welcome back, Admin! Please log in to continue.</div>
        <h2>Admin Login</h2>
        <form method="POST" action="">
            <div class="input-container">
                <img src="../img/username-icon.png" alt="Email Icon">
                <input type="email" name="email" placeholder="Email" required>
            </div>
            <div class="input-container">
                <img src="../img/password-icon.png" alt="Password Icon">
                <input type="password" name="password" id="password" placeholder="Password" required>
                <img src="../img/hide-eye-icon.png" alt="Toggle Password Visibility" class="toggle-password" id="togglePassword" onclick="togglePasswordVisibility()">
            </div>
            <input type="submit" value="Login" class="submit-btn">
        </form>
        <div class="forgot-password">
            <a href="../php/forgot-password.php">Forgot Password?</a>
        </div>
        <div id="error-message" class="error-message <?php echo !empty($error_message) ? 'show-error' : ''; ?>">
            <?php echo $error_message; ?>
        </div>
    </div>
    <div class="right-image">
        <img src="../img/memorix-bg.png" alt="Right Side Image"> 
    </div>
</div>

<script>
    window.onload = function() {
        var errorMessage = document.getElementById('error-message');
        var emailInput = document.querySelector('input[name="email"]');
        var passwordInput = document.querySelector('input[name="password"]');
        
        if (errorMessage.classList.contains('show-error')) {
            emailInput.style.border = "1px solid #ff0000";
            passwordInput.style.border = "1px solid #ff0000";

            setTimeout(function() {
                errorMessage.classList.remove('show-error');
                emailInput.style.border = "";
                passwordInput.style.border = "";
            }, 5000);
        }
    };

    function togglePasswordVisibility() {
        const passwordInput = document.getElementById('password');
        const toggleIcon = document.getElementById('togglePassword');

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            toggleIcon.src = '../img/show-eye-icon.png'; 
        } else {
            passwordInput.type = 'password';
            toggleIcon.src = '../img/hide-eye-icon.png'; 
        }
    }
</script>
</body>
</html>
