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

if (!isset($_POST['id_cita'])) {
    echo json_encode(["success" => false, "message" => "ID de cita no proporcionado"]);
    exit;
}

$id_cita = $conn->real_escape_string($_POST['id_cita']);

$sql = "DELETE FROM citas WHERE id_cita = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id_cita);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["success" => true, "message" => "Cita eliminada correctamente"]);
    } else {
        echo json_encode(["success" => false, "message" => "No se encontró la cita con ese ID"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Error al eliminar cita: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>