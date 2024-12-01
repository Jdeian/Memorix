<?php
include "database.php"; 
include "delete-notice.php";

error_reporting(E_ALL);
ini_set('display_errors', '1');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $delete_user_id = $_POST['delete_user_id'];

    $user_query = "SELECT email, fullname FROM users WHERE id=?";
    $stmt = $conn->prepare($user_query);

    if (!$stmt) {
        echo json_encode(['error' => true, 'message' => 'Statement preparation failed: ' . $conn->error]);
        exit();
    }

    $stmt->bind_param("i", $delete_user_id);
    $stmt->execute();
    $stmt->bind_result($userEmail, $userName);
    $stmt->fetch();
    $stmt->close();

    if (!$userEmail) {
        echo json_encode(['error' => true, 'message' => 'User not found']);
        exit();
    }

    if (!sendAccountDeletionEmail($userEmail, $userName)) {
        echo json_encode(['error' => true, 'message' => 'Failed to send notification, user deletion aborted']);
        exit();
    }

    $delete_query = "DELETE FROM users WHERE id=?";
    $stmt = $conn->prepare($delete_query);

    if (!$stmt) {
        echo json_encode(['error' => true, 'message' => 'Statement preparation failed: ' . $conn->error]);
        exit();
    }

    $stmt->bind_param("i", $delete_user_id);

    if ($stmt->execute()) {
        echo json_encode(['error' => false, 'message' => 'User deleted successfully and notification sent']);
    } else {
        echo json_encode(['error' => true, 'message' => 'Error deleting user: ' . $stmt->error]);
    }

    $stmt->close();
    exit();
}
?>

<div id="deleteUserModal" class="modal-delete">
    <div class="modal-content-delete">
        <span class="close" onclick="closeDeleteModal()">&times;</span>
        <h2>Confirm Deletion</h2>
        <p style="color: #AEB9E1;">Do you want to delete this user?</p>
        <div class="modal-actions">
            <button id="confirmDeleteBtn" onclick="deleteUser()">Yes, Delete</button>
            <button onclick="closeDeleteModal()">Cancel</button>
        </div>
    </div>
</div>

<style>
.modal-delete {
    display: none;
    position: fixed;
    z-index: 1000; 
    left: 0;
    top: 0;
    width: 100%; 
    height: 100%; 
    background-color: rgba(0,0,0,0.7); 
    font-family: 'Work Sans', sans-serif;
}
.modal-delete::-webkit-scrollbar {
    display: none;
}
.modal-content-delete {
    background-color: #081028;
    margin: 20% auto;
    padding: 20px;
    border: 1px solid #343B4F;
    width: 30%; 
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    border-radius: 8px; 
}
.modal-actions {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
}
.modal-actions button {
    padding: 10px 20px;
    border: none;
    cursor: pointer;
    font-size: 16px;
}
.modal-actions button:first-child {
    background-color: red;
    color: white;
    border-radius: 5px;
}
.modal-actions button:last-child {
    background-color: #ccc;
    color: black;
    border-radius: 5px;
}
.modal-content-delete p, h2 {
    color: #AEB9E1;
    font-size: 18px;
    margin-bottom: 5px;
}
</style>

<script>
let currentUserId;

function openDeleteModal(userId) {
    currentUserId = userId; 
    document.getElementById('deleteUserModal').style.display = 'block';
}

function closeDeleteModal() {
    document.getElementById('deleteUserModal').style.display = 'none';
}

function deleteUser() {
    const spinner = document.querySelector('.spinner');
    spinner.style.display = 'block'; 

    const formData = new FormData();
    formData.append('delete_user_id', currentUserId); 

    fetch('../includes/delete_user.php', {
        method: 'POST',
        body: formData,
    })
    .then(response => {
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return response.json();
        } else {
            return response.text().then(text => { throw new Error(text); });
        }
    })
    .then(data => {
    if (!data.error) {
        sessionStorage.setItem('deleteUserMessage', data.message);
        window.location.href = 'user.php';
    } else {
        alert(data.message); 
    }
    closeDeleteModal(); 
    })
    .catch(error => {
        console.error('Error details:', error);
        window.location.href = 'user.php';
    })
    .finally(() => {
        spinner.style.display = 'none'; 
    });
}


document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById("deleteUserModal");
    
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeDeleteModal();
        }
    });
});
</script>
