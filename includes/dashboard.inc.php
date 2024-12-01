<?php
include "../includes/database.php";

error_reporting(E_ALL);
ini_set('display_errors', 1);

$year = isset($_GET['year']) ? intval($_GET['year']) : date('Y'); 

$freeUsers = array_fill(0, 12, 0);
$basicUsers = array_fill(0, 12, 0);
$premiumUsers = array_fill(0, 12, 0);

$currentYear = date('Y');

$mockData2023 = [
    'free' => [50, 60, 30, 80, 40, 70, 50, 100, 80, 60, 150, 120],
    'basic' => [20, 40, 90, 25, 50, 45, 60, 30, 70, 55, 30, 50],
    'premium' => [10, 15, 20, 15, 28, 20, 32, 25, 36, 30, 40, 18],
];

if ($year === 2023) {
    $freeUsers = $mockData2023['free'];
    $basicUsers = $mockData2023['basic'];
    $premiumUsers = $mockData2023['premium'];
} else {
    if ($year == $currentYear) {
        $queryCurrentYear = "
            SELECT MONTH(created_at) AS month, category, COUNT(*) AS user_count 
            FROM users 
            WHERE YEAR(created_at) IN ($currentYear, " . ($currentYear - 1) . ") 
            GROUP BY MONTH(created_at), category
        ";

        $resultCurrentYear = mysqli_query($conn, $queryCurrentYear);

        while ($row = mysqli_fetch_assoc($resultCurrentYear)) {
            $month = intval($row['month']) - 1;
            switch ($row['category']) {
                case 'Free':
                    $freeUsers[$month] += intval($row['user_count']);
                    break;
                case 'Basic':
                    $basicUsers[$month] += intval($row['user_count']);
                    break;
                case 'Premium':
                    $premiumUsers[$month] += intval($row['user_count']);
                    break;
            }
        }
    } else {
        $query = "
            SELECT MONTH(created_at) AS month, category, COUNT(*) AS user_count 
            FROM users 
            WHERE YEAR(created_at) = $year 
            GROUP BY MONTH(created_at), category
        ";

        $result = mysqli_query($conn, $query);

        while ($row = mysqli_fetch_assoc($result)) {
            $month = intval($row['month']) - 1;
            switch ($row['category']) {
                case 'Free':
                    $freeUsers[$month] = intval($row['user_count']);
                    break;
                case 'Basic':
                    $basicUsers[$month] = intval($row['user_count']);
                    break;
                case 'Premium':
                    $premiumUsers[$month] = intval($row['user_count']);
                    break;
            }
        }
    }
}

header('Content-Type: application/json');
$response = [
    'freeUsers' => $freeUsers,
    'basicUsers' => $basicUsers,
    'premiumUsers' => $premiumUsers,
];

echo json_encode($response);
?>
