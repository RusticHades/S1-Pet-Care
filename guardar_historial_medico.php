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

// Obtener datos del POST
$id_mascota = $_POST['id_mascota'] ?? '';
$id_veterinario = $_POST['id_veterinario'] ?? '';
$fecha_consulta = $_POST['fecha_consulta'] ?? '';
$motivo_consulta = $_POST['motivo_consulta'] ?? '';
$diagnostico = $_POST['diagnostico'] ?? '';
$tratamiento = $_POST['tratamiento'] ?? '';
$observaciones = $_POST['observaciones'] ?? '';
$peso_actual = $_POST['peso_actual'] ?? null;
$temperatura = $_POST['temperatura'] ?? null;
$vacunas_aplicadas = $_POST['vacunas_aplicadas'] ?? '';

// Validar campos obligatorios
if (empty($id_mascota) || empty($id_veterinario) || empty($fecha_consulta) || 
    empty($motivo_consulta) || empty($diagnostico)) {
    echo json_encode(["success" => false, "message" => "Faltan campos obligatorios"]);
    exit;
}

// Convertir valores numéricos
$peso_actual = !empty($peso_actual) ? floatval($peso_actual) : null;
$temperatura = !empty($temperatura) ? floatval($temperatura) : null;

// Preparar la consulta SQL
$sql = "INSERT INTO historial_medico 
    (id_mascota, id_veterinario, fecha_consulta, motivo_consulta, diagnostico, 
     tratamiento, observaciones, peso_actual, temperatura, vacunas_aplicadas, fecha_registro) 
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta: " . $conn->error]);
    exit;
}

// Verificar tipos de datos correctos para bind_param
// i=int, d=double, s=string
$stmt->bind_param("iisssssdds", 
    $id_mascota, $id_veterinario, $fecha_consulta, $motivo_consulta, $diagnostico,
    $tratamiento, $observaciones, $peso_actual, $temperatura, $vacunas_aplicadas);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["success" => true, "message" => "Historial médico guardado con éxito"]);
    } else {
        echo json_encode(["success" => false, "message" => "No se insertaron filas"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Error al ejecutar: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>