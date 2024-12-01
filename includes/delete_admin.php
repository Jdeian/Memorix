<?php
include 'database.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['delete_admin']) && $_POST['delete_admin'] == '1') {
    $admin_id = isset($_POST['admin_id']) ? intval($_POST['admin_id']) : 0;

    if ($admin_id > 0) {
        $delete_query = $conn->prepare("DELETE FROM admin WHERE admin_id = ?");
        $delete_query->bind_param("i", $admin_id);

        if ($delete_query->execute()) {
            header("Location: ../php/admin-settings.php");
            exit();
        } else {
            header("Location: ../php/admin-settings.php");
            exit();
        }
    } else {
        header("Location: ../php/admin-settings.php");
        exit();
    }
}
?>

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
    margin-top: 10px;
    background-color: red;
    color: white;
    border: 0 solid #343B4F;
    border-radius: 5px;
}

.modal-actions button:last-child {
    margin-top: 10px;
    background-color: #ccc;
    color: black;
    border: 0 solid #343B4F;
    border-radius: 5px;
}

.modal-content-delete p, h2 {
    color: #AEB9E1;;
    font-size: 18px;
    margin-bottom: 5px;
}

</style>



<div id="deleteModal" class="modal-delete">
    <div class="modal-content-delete">
        <span class="close" onclick="closeDeleteModal()">&times;</span>
        <h2>Confirm Deletion</h2>
        <p>Are you sure you want to delete this admin?</p>
        <input type="hidden" id="delete_admin_id" value="">
        <div class="modal-actions">
        <button id="confirmDelete" onclick="confirmDelete()">Yes, Delete</button>
        <button onclick="closeDeleteModal()">Cancel</button>
        </div>
    </div>
</div>

<script>

function openDeleteModal(admin_id) {
    document.getElementById('delete_admin_id').value = admin_id; 
    document.getElementById('deleteModal').style.display = 'block'; 
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none'; 
}

function confirmDelete() {
    const admin_id = document.getElementById('delete_admin_id').value; 

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '../includes/delete_admin.php'; 

    const hiddenField = document.createElement('input');
    hiddenField.type = 'hidden';
    hiddenField.name = 'delete_admin';
    hiddenField.value = '1'; 

    const idField = document.createElement('input');
    idField.type = 'hidden';
    idField.name = 'admin_id';
    idField.value = admin_id;

    form.appendChild(hiddenField);
    form.appendChild(idField);
    document.body.appendChild(form); 
    form.submit(); 
}

</script>
