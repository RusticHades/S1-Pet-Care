<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Conexión fallida"]);
    exit;
}

$id_mascota = $_GET['id_mascota'];

$sql = "SELECT * FROM medicamentos WHERE id_mascota = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id_mascota);
$stmt->execute();
$result = $stmt->get_result();

$medicamentos = [];
while ($row = $result->fetch_assoc()) {
    $medicamentos[] = $row;
}

echo json_encode(["success" => true, "medicamentos" => $medicamentos]);
$conn->close();
?>