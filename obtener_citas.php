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

if (!isset($_GET['id_mascota'])) {
    echo json_encode(["success" => false, "message" => "ID de mascota no proporcionado"]);
    exit;
}

$id_mascota = $conn->real_escape_string($_GET['id_mascota']);

$sql = "SELECT id_cita, DATE_FORMAT(fecha_cita, '%Y/%m/%d %H:%i') as fecha_hora, motivo, notas, estado 
        FROM citas 
        WHERE id_mascota = ? 
        ORDER BY fecha_cita ASC";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id_mascota);
$stmt->execute();
$result = $stmt->get_result();

$citas = array();
while ($row = $result->fetch_assoc()) {
    $citas[] = $row;
}

if (count($citas) > 0) {
    echo json_encode(["success" => true, "citas" => $citas]);
} else {
    echo json_encode(["success" => true, "citas" => [], "message" => "No se encontraron citas"]);
}

$stmt->close();
$conn->close();
?>