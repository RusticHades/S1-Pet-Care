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

$auth = new Auth();
$token = $auth->getTokenFromHeaders();

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
$estados = isset($_GET['estado']) ? explode(',', $_GET['estado']) : ['pendiente', 'confirmada'];

$placeholders = implode(',', array_fill(0, count($estados), '?'));
$types = str_repeat('s', count($estados));
$params = $estados;
array_unshift($params, $idMascota);

$query = "SELECT * FROM citas WHERE id_mascota = ? AND estado IN ($placeholders) ORDER BY fecha_hora ASC";
$stmt = $conn->prepare($query);
$stmt->bind_param("i" . $types, ...$params);
$stmt->execute();
$result = $stmt->get_result();

$citas = [];
while ($row = $result->fetch_assoc()) {
    $citas[] = $row;
}

echo json_encode($citas);

$stmt->close();
$conn->close();
?>