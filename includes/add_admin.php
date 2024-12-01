<?php

include "../includes/database.php";

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['add_admin']) && $_POST['add_admin'] == '1') {
    $name = isset($_POST['admin-name']) ? $_POST['admin-name'] : '';
    $email = isset($_POST['admin-email']) ? $_POST['admin-email'] : '';
    $password = isset($_POST['admin-password']) ? $_POST['admin-password'] : '';
    $number = isset($_POST['admin-number']) ? $_POST['admin-number'] : '';
    $role = isset($_POST['admin-role']) ? $_POST['admin-role'] : '';
    $address = isset($_POST['admin-address']) ? $_POST['admin-address'] : '';
    

    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
    $sql = "INSERT INTO admin (name, email, password, number, role, address) VALUES (?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);

    if ($stmt === false) {
        die("Error preparing the SQL statement: " . $conn->error);
    }

    $stmt->bind_param("ssssss", $name, $email, $hashedPassword, $number, $role, $address);

    if ($stmt->execute()) {
        header("Location: ../php/admin-settings.php");
        exit;
    } else {
        echo 'Failed to add user: ' . $stmt->error;
    }

    $stmt->close();
}

$conn->close();
?>

<div id="addAdminModal" class="addAdminModal">
    <div class="add-admin-modal-content">
        <span class="close" onclick="closeAddModal()">&times;</span>
        <h2>Add Admin</h2>
        <form id="addAdminForm" method="POST" action="../includes/add_admin.php">
        <input type="hidden" name="add_admin" value="1">
            <div class="form-group">
                <label for="fullname">Full Name:</label>
                <input type="text" name="admin-name" id="modalFullName" required>
            </div>
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" name="admin-email" id="modalEmail" required>
            </div>
            <div class="form-group">
                <label for="status">Phone Number:</label>
                <input type="tel" name="admin-number"  pattern="[0-9]{11}" id="modalNumber" required>
            </div>
            <div class="form-group">
                <label for="adminAddress">Address:</label>
                <input type="text" name="admin-address" id="addModalAdminAddress" required>
            </div>
            <div class="form-group">
                <label for="adminRole">Role:</label>
                <select name="admin-role" id="addModalAdminRole" required>
                    <option value="">Select Role</option>
                    <option value="Super Admin">Super Admin</option>
                    <option value="Moderator">Moderator</option>
                    <option value="Support">Support</option>
                </select>
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" name="admin-password" id="modalPassword" required>
                <img src="../img/white-hide-eye.png" alt="Toggle Password Visibility" class="toggle-password" id="togglePassword" onclick="togglePasswordVisibility()">
            </div>
            <div class="form-group">
                <label for="confirm-password">Confirm Password:</label>
                <input type="password" name="admin-confirmpassword" id="modalConfirmPassword" required>
                <img src="../img/white-hide-eye.png" alt="Toggle Password Visibility" class="toggle-password" id="toggleConfirmPassword" onclick="toggleConfirmPasswordVisibility()">
            </div>
            <div class="add-button-container"><button type="submit" class="add-admin-button">Add Admin</button></div>
        </form>
        <div id="error-message" class="error-message">Error Message</div>
    </div>
</div>

<style>
.addAdminModal {
    display: none;
    position: fixed;
    z-index: 1000; 
    left: 0;
    top: 0;
    width: 100%; 
    height: 100%; 
    background-color: rgb(0,0,0); 
    background-color: rgba(0,0,0,0.7); 
    font-family: 'Work Sans', sans-serif;
    overflow-y: auto;
}

.addAdminModal::-webkit-scrollbar {
   display: none;
}

.add-admin-modal-content {
    background-color: #fefefe;
    margin: 2.5% auto;
    padding: 20px;
    border: 1px solid #343B4F;
    width: 50%; 
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    border-radius: 8px; 
    background-color: #081028;
    overflow-y: auto;
}

.add-admin-modal-content h2 {
    color: white;
    margin-top: 20px;
    margin-bottom: 20px;
    font-weight: 400;
}

.close {
    color: #aaa;
    float: right;
    font-size: 28px;
    font-weight: bold;
    cursor: pointer;
}

.form-group {
    position: relative;
}

#modalAdminId, #modalFullName, #modalNumber, #modalEmail, #addModalAdminAddress, #addModalAdminRole, #modalPassword, #modalConfirmPassword, input[type="email"]{
    width: 100%;
    padding: 10px; 
    margin: 10px 0; 
    margin-bottom: 20px;
    border-radius: 4px; 
    box-sizing: border-box;
    font-family: 'Work Sans', sans-serif;
    background-color: #0B1739;
    border: 1px solid #343B4F;
    color: white;
}

.toggle-password {
    position: absolute;
    right: 10px;
    top: 55%;
    transform: translateY(-50%);
    cursor: pointer;
    width: 20px;
    height: 20px;
    z-index: 1;
}

label {
    color: white;
}

.add-button-container {
    display: flex; 
    justify-content: center; 
    margin-top: 20px; 
}

.add-admin-button {
    display: flex;
    justify-content: center; 
    padding: 10px 20px;
    background-color: #4CAF50;
    color: white; 
    border: none;
    border-radius: 4px; 
    cursor: pointer; 
    font-size: 16px; 
    background-color: #16285c;
    transition: background-color 0.3s;
    font-family:  'Work Sans', sans-serif;
}

.add-admin-button:hover {
    background-color: #AEB9E1;
    color: black;
}

@keyframes errorAnimation {
    0% {
        opacity: 1; 
    }
    50% {
        opacity: 1; 
    }
    100% {
        opacity: 0;
    }
}

.error-message {
    display: none; 
    position: fixed;
    top: 0;
    left: 50%;
    width: cover;
    transform: translateX(-50%);
    background-color: red;
    color: #fff;
    padding: 10px;
    border-radius: 4px;
    animation: errorAnimation 5s ease;
    opacity: 0;
    margin-top: 10px; 
}

</style>

<script>
function openAddUserModal() {
    document.getElementById("addAdminModal").style.display = "block";
}

function closeAddModal() {
    document.getElementById("addAdminModal").style.display = "none";
}

document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById("addAdminModal");
    
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeAddModal();
        }
    });
});

function togglePasswordVisibility() {
    const passwordInput = document.getElementById('modalPassword');
    const toggleIcon = document.getElementById('togglePassword');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.src = '../img/white-show-eye.png'; 
    } else {
        passwordInput.type = 'password';
        toggleIcon.src = '../img/white-hide-eye.png';  
    }
}

function toggleConfirmPasswordVisibility() {
    const passwordInput = document.getElementById('modalConfirmPassword');
    const toggleIcon = document.getElementById('toggleConfirmPassword');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.src = '../img/white-show-eye.png'; 
    } else {
        passwordInput.type = 'password';
        toggleIcon.src = '../img/white-hide-eye.png';  
    }
}

const errorMessage = document.querySelector('.error-message');
const password = document.getElementById('modalPassword');
const confirmPassword = document.getElementById('modalConfirmPassword');
const addBtn = document.querySelector('.add-admin-button');
const emailInput = document.getElementById('modalEmail'); 

addBtn.addEventListener('click', (event) => {
    event.preventDefault(); 
    const passwordValue = password.value.trim();
    const confirmPasswordValue = confirmPassword.value.trim();
    const emailValue = emailInput.value.trim();

    confirmPassword.style.border = "";
    password.style.border = "";
    errorMessage.style.display = 'none';

    const requiredFields = [modalFullName, emailInput, modalNumber, modalPassword, confirmPassword, addModalAdminAddress, addModalAdminRole];
    let allFilled = true;

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.style.border = "1px solid red"; 
            errorMessage.textContent = 'Fill all the fields';
            errorMessage.style.display = 'flex';
            allFilled = false; 
        } else {
            field.style.border = ""; 
        }
    });

    if (allFilled) {
        fetch('../includes/check_email.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `admin-email=${encodeURIComponent(emailValue)}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.exists) {
                emailInput.style.border = "1px solid red";
                errorMessage.textContent = 'Email already exists';
                errorMessage.style.display = 'flex';
            } else {
                if (passwordValue !== "" && passwordValue.length < 8) {
                    password.style.border = "1px solid red";
                    errorMessage.textContent = 'Password must be at least 8 characters long';
                    errorMessage.style.display = 'flex';
                } else if (confirmPasswordValue !== "" && passwordValue !== confirmPasswordValue) {
                    confirmPassword.style.border = "1px solid red";
                    errorMessage.textContent = 'Passwords do not match';
                    errorMessage.style.display = 'flex';
                } else {
                    document.getElementById("addAdminForm").submit();
                }
            }
        })
        .catch(err => {
            console.error('Error checking email:', err);
        });
    }
});

</script>

