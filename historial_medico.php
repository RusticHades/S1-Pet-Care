<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["error" => "Connection failed: " . $conn->connect_error]));
}

if (!$auth->validarToken($token)) {
    http_response_code(401);
    echo json_encode(['error' => 'Token inválido o expirado']);
    exit;
}

if (!isset($_GET['id_mascota'])) {
    http_response_code(400);
    echo json_encode(['error' => 'ID de mascota no proporcionado']);
    exit;
}

$idMascota = $_GET['id_mascota'];

$stmt = $conn->prepare("SELECT * FROM historial_medico WHERE id_mascota = ? ORDER BY fecha_consulta DESC");
$stmt->bind_param("i", $idMascota);
$stmt->execute();
$result = $stmt->get_result();

$historial = [];
while ($row = $result->fetch_assoc()) {
    $historial[] = $row;
}

echo json_encode($historial);

$stmt->close();
$conn->close();
?>