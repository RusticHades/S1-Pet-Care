<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

// Conectar a MySQL
$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}

// Obtener ID de mascota
$id_mascota = $_GET['id_mascota'] ?? '';

if (empty($id_mascota)) {
    echo json_encode(["success" => false, "message" => "ID de mascota no proporcionado"]);
    exit;
}

// Consulta SQL para obtener historial médico
$sql = "SELECT * FROM historial_medico WHERE id_mascota = ? ORDER BY fecha_consulta DESC";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id_mascota);
$stmt->execute();
$result = $stmt->get_result();

$historial = [];
while ($row = $result->fetch_assoc()) {
    // Formatear fecha si es necesario
    $fecha_consulta = date("d/m/Y", strtotime($row['fecha_consulta']));
    $row['fecha_consulta'] = $fecha_consulta;
    
    $historial[] = $row;
}

echo json_encode($historial);

$stmt->close();
$conn->close();
?>