<?php

include "../includes/database.php";

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['add_user']) && $_POST['add_user'] == '1') {
    $fullname = isset($_POST['add-fullname']) ? $_POST['add-fullname'] : '';
    $birthdate = isset($_POST['add-birthdate']) ? $_POST['add-birthdate'] : '';
    $nickname = isset($_POST['add-nickname']) ? $_POST['add-nickname'] : '';
    $email = isset($_POST['add-email']) ? $_POST['add-email'] : '';
    $password = isset($_POST['add-password']) ? $_POST['add-password'] : '';
    $category = isset($_POST['add-category']) ? $_POST['add-category'] : '';
    $status = isset($_POST['add-status']) ? $_POST['add-status'] : '';

    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
    $sql = "INSERT INTO users (fullname, birthdate, nickname, email, password, category, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 1 DAY))";
    $stmt = $conn->prepare($sql);

    if ($stmt === false) {
        die("Error preparing the SQL statement: " . $conn->error);
    }

    $stmt->bind_param("sssssss", $fullname, $birthdate, $nickname, $email, $hashedPassword, $category, $status);

    if ($stmt->execute()) {
        header("Location: ../php/user.php");
        exit;
    } else {
        echo 'Failed to add user: ' . $stmt->error;
    }

    $stmt->close();
}

$conn->close();
?>

<div id="addUserModal" class="addUserModal">
    <div class="add-user-modal-content">
        <span class="close" onclick="closeAddModal()">&times;</span>
        <h2>Add User</h2>
        <form id="addUserForm" method="POST" action="../includes/add_user.php">
        <input type="hidden" name="add_user" value="1">
            <div class="form-group">
                <label for="fullname">Full Name:</label>
                <input type="text" name="add-fullname" id="addModalFullName" required>
            </div>
            <div class="form-group">
                <label for="birthdate">Birthdate:</label>
                <input type="date" name="add-birthdate" id="addModalBirthdate">
            </div>
            <div class="form-group">
                <label for="nickname">Nickname:</label>
                <input type="text" name="add-nickname" id="addModalNickname">
            </div>
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" name="add-email" id="addModalEmail" required>
            </div>
            <div class="form-group">
                <label for="category">Category:</label>
                <select name="add-category" id="addModalCategory">
                    <option value="Free">Free</option>
                    <option value="Basic">Basic</option>
                    <option value="Premium">Premium</option>
                </select>
            </div>
            <div class="form-group">
                <label for="status">Status:</label>
                <select name="add-status" id="addModalStatus">
                    <option value="Active">Active</option>
                    <option value="Inactive">Inactive</option>
                </select>
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" name="add-password" id="addModalPassword" required>
                <img src="../img/white-hide-eye.png" alt="Toggle Password Visibility" class="toggle-password" id="togglePassword" onclick="togglePasswordVisibility()">
            </div>
            <div class="form-group">
                <label for="confirm-password">Confirm Password:</label>
                <input type="password" name="add-confirm-password" id="addModalConfirmPassword" required>
                <img src="../img/white-hide-eye.png" alt="Toggle Password Visibility" class="toggle-password" id="toggleConfirmPassword" onclick="toggleConfirmPasswordVisibility()">
            </div>
            <div class="add-button-container"><button type="submit" class="add-button">Add User</button></div>
        </form>
        <div id="error-message" class="error-message">Error Message</div>
    </div>
</div>

<style>
.addUserModal {
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

.addUserModal::-webkit-scrollbar {
   display: none;
}

.add-user-modal-content {
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

.add-user-modal-content h2 {
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

#modalUserId, #addModalFullName, #addModalBirthdate, #addModalNickname, #addModalEmail, #addModalPassword, #addModalConfirmPassword {
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

input[type="date"]::-webkit-calendar-picker-indicator {
    filter: invert(1); 
    cursor: pointer;
}

textarea {
    width: 100%;
    padding: 10px;
    margin: 10px 0; 
    border: 1px solid #ccc; 
    border-radius: 4px; 
    box-sizing: border-box; 
}

label {
    color: white;
}

.add-button-container {
    display: flex; 
    justify-content: center; 
    margin-top: 20px; 
}

.add-button {
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

.add-button:hover {
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
    document.getElementById("addUserModal").style.display = "block";
}

function closeAddModal() {
    document.getElementById("addUserModal").style.display = "none";
}

document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById("addUserModal");
    
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeAddModal();
        }
    });
});

function togglePasswordVisibility() {
    const passwordInput = document.getElementById('addModalPassword');
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
    const passwordInput = document.getElementById('addModalConfirmPassword');
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
const password = document.getElementById('addModalPassword');
const confirmPassword = document.getElementById('addModalConfirmPassword');
const addBtn = document.querySelector('.add-button');
const emailInput = document.getElementById('addModalEmail'); 

addBtn.addEventListener('click', (event) => {
    event.preventDefault(); 
    const passwordValue = password.value.trim();
    const confirmPasswordValue = confirmPassword.value.trim();
    const emailValue = emailInput.value.trim();

    confirmPassword.style.border = "";
    password.style.border = "";
    errorMessage.style.display = 'none';

    const requiredFields = [addModalFullName, emailInput, addModalBirthdate, addModalNickname, addModalPassword, addModalConfirmPassword];
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
        fetch('../includes/check_user_email.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `user-email=${encodeURIComponent(emailValue)}`
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
                    document.getElementById("addUserForm").submit();
                }
            }
        })
        .catch(err => {
            console.error('Error checking email:', err);
        });
    }
});

</script>

