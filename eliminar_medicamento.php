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

$id_medicamento = $_POST['id_medicamento'];

$sql = "DELETE FROM medicamentos WHERE id_medicamento = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id_medicamento);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "message" => "No se pudo eliminar"]);
}
$conn->close();
?>