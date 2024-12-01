

<div id="editUserModal" class="modal">
    <div class="modal-content">
        <span class="close" onclick="closeModal()">&times;</span>
        <h2>Edit User</h2>
        <form id="editUserForm" method="POST" action="../php/user.php">
            <input type="hidden" name="user_id" id="modalUserId">
            <div class="form-group">
                <label for="fullname">Full Name:</label>
                <input type="text" name="fullname" id="modalFullName" required>
            </div>
            <div class="form-group">
                <label for="birthdate">Birthdate:</label>
                <input type="date" name="birthdate" id="modalBirthdate">
            </div>
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" name="email" id="modalEmail" required>
            </div>
            <div class="form-group">
                <label for="category">Category:</label>
                <select name="category" id="modalCategory">
                    <option value="Free">Free</option>
                    <option value="Basic">Basic</option>
                    <option value="Premium">Premium</option>
                </select>
            </div>
            <div class="form-group">
                <label for="status">Status:</label>
                <select name="status" id="modalStatus">
                    <option value="Active">Active</option>
                    <option value="Inactive">Inactive</option>
                </select>
            </div>
            <div class="update-button-container"><button type="submit" class="update-button">Update User</button></div>
        </form>
    </div>
</div>

<style>
.modal {
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

#modalUserId, #modalFullName, #modalBirthdate, #modalEmail, input[type="email"], select, #modalBirthdate {
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



.update-button-container {
    display: flex; 
    justify-content: center; 
    margin-top: 20px; 
}

.update-button {
    display: flex;
    justify-content: center; 
    margin-top: 20px; 
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

.update-button:hover {
    background-color: #AEB9E1;
    color: black;
}

</style>

<script>
function openModal(id, fullname, birthdate, email, category, status) {
    document.getElementById("modalUserId").value = id;
    document.getElementById("modalFullName").value = fullname;
    document.getElementById("modalBirthdate").value = birthdate;
    document.getElementById("modalEmail").value = email;
    document.getElementById("modalCategory").value = category;
    document.getElementById("modalStatus").value = status;

    document.getElementById("editUserModal").style.display = "block";
}

function closeModal() {
    document.getElementById("editUserModal").style.display = "none";
}

document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById("editUserModal");
    
    modal.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeModal();
        }
    });
});
</script>

