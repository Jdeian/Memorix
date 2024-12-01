<?php
ob_start(); // Start output buffering

include '../includes/database.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['edit_admin'])) {
    $admin_id = isset($_POST['admin_id']) ? intval($_POST['admin_id']) : 0;
    $name = isset($_POST['name']) ? $_POST['name'] : '';
    $email = isset($_POST['email']) ? $_POST['email'] : '';
    $short_description = isset($_POST['short_description']) ? $_POST['short_description'] : '';
    $number = isset($_POST['number']) ? $_POST['number'] : '';
    $role = isset($_POST['role']) ? $_POST['role'] : '';
    $address = isset($_POST['address']) ? $_POST['address'] : '';

    if ($admin_id > 0) {
        $update_query = $conn->prepare("UPDATE admin SET name = ?, email = ?, short_description = ?, number = ?, role = ?, address = ? WHERE admin_id = ?");
        $update_query->bind_param("ssssssi", $name, $email, $short_description, $number, $role, $address, $admin_id);

        if ($update_query->execute()) {
            header("Location: ../php/admin-settings.php");
            exit();
        } else {
            header("Location: ../php/admin-settings.php");
            exit();
        }
    }
}

ob_end_flush(); // Flush the output buffer
?>

<div id="editAdminModal" class="modal">
    <div class="modal-content">
        <span class="close" onclick="closeAdminModal()">&times;</span>
        <h2>Edit Admin</h2>
        <form id="editAdminForm" method="POST" action="../includes/edit_admin.php">
            <input type="hidden" name="admin_id" id="modalAdminId">
            <div class="form-group">
                <label for="adminName">Name:</label>
                <input type="text" name="name" id="modalAdminName" required>
            </div>
            <div class="form-group">
                <label for="adminEmail">Email:</label>
                <input type="email" name="email" id="modalAdminEmail" required>
            </div>
            <div class="form-group">
                <label for="adminShortDescription">Short Description:</label>
                <textarea name="short_description" id="modalAdminShortDescription" rows="3"></textarea>
            </div>
            <div class="form-group">
                <label for="adminNumber">Contact Number:</label>
                <input type="text" name="number" id="modalAdminNumber">
            </div>
            <div class="form-group">
                <label for="adminRole">Role:</label>
                <select name="role" id="modalAdminRole">
                    <option value="Super Admin">Super Admin</option>
                    <option value="Moderator">Moderator</option>
                    <option value="Support">Support</option>
                </select>
            </div>
            <div class="form-group">
                <label for="adminAddress">Address:</label>
                <input type="text" name="address" id="modalAdminAddress">
            </div>
            <div class="update-button-container">
                <button type="submit" class="update-button" name="edit_admin">Update Admin</button>
            </div>
        </form>
    </div>
</div>

<style>
.modal {
    display: none;
    position: absolute;
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

.modal::-webkit-scrollbar {
  display: none;
}

.modal-content {
    background-color: #fefefe;
    margin: 2.5% auto;
    padding: 20px;
    border: 1px solid #343B4F;
    width: 50%; 
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    border-radius: 8px; 
    background-color: #081028;
}

.modal-content h2 {
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

#modalAdminId, #modalAdminName, #modalAdminEmail, #modalAdminShortDescription, #modalAdminNumber, #modalAdminRole, #modalAdminAddress {
    width: 100%;
    padding: 10px; 
    margin: 10px 0; 
    margin-bottom: 20px;
    background-color: #0B1739;
    border: 1px solid #343B4F;
    color: white;
    border-radius: 4px; 
    box-sizing: border-box;
    font-family: 'Work Sans', sans-serif;
}

label {
    color: white;
}

.update-button-container {
    display: flex; 
    justify-content: center; 
    margin-top: 20px; 
}

.update-button {
    display: flex;
    justify-content: center; 
    padding: 10px 20px;
    background-color: #16285c;
    color: white; 
    border: none;
    border-radius: 4px; 
    cursor: pointer; 
    font-size: 16px; 
    transition: background-color 0.3s;
}

.update-button:hover {
    background-color: #AEB9E1;
    color: black;
}
</style>

<script>
function openAdminModal(id, name, email, shortDescription, number, role, address) {
    document.getElementById("modalAdminId").value = id;
    document.getElementById("modalAdminName").value = name;
    document.getElementById("modalAdminEmail").value = email;
    document.getElementById("modalAdminShortDescription").value = shortDescription;
    document.getElementById("modalAdminNumber").value = number;
    document.getElementById("modalAdminRole").value = role;
    document.getElementById("modalAdminAddress").value = address;

    document.getElementById("editAdminModal").style.display = "block";
}

function closeAdminModal() {
    document.getElementById("editAdminModal").style.display = "none";
}

document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById("editAdminModal");
    
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeAdminModal();
        }
    });
});
</script>
