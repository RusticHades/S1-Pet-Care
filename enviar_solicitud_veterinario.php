<?php
header('Content-Type: application/json');

// Verificar que sea una solicitud POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Método no permitido']);
    exit;
}

// Verificar que todos los campos requeridos estén presentes
$required_fields = ['usuario_id', 'especialidad', 'ubicacion', 'telefono'];
foreach ($required_fields as $field) {
    if (empty($_POST[$field])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => "El campo $field es requerido"]);
        exit;
    }
}

// Verificar que se haya subido un archivo
if (!isset($_FILES['certificado'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Certificado no recibido']);
    exit;
}

// Configuración de la base de datos
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error de conexión a la base de datos: ' . $conn->connect_error
    ]);
    exit;
}

// Procesar el archivo
$certificado = $_FILES['certificado'];
$nombreArchivo = uniqid() . '_' . basename($certificado['name']);
$rutaDestino = 'certificados/' . $nombreArchivo;

// Crear directorio si no existe
if (!file_exists('certificados')) {
    mkdir('certificados', 0777, true);
}

if (!move_uploaded_file($certificado['tmp_name'], $rutaDestino)) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error al subir el certificado']);
    exit;
}

// Insertar en la base de datos
$stmt = $conn->prepare("INSERT INTO solicitudes_veterinarios 
                       (usuario_id, especialidad, certificado, ubicacion, telefono) 
                       VALUES (?, ?, ?, ?, ?)");
if (!$stmt) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error al preparar la consulta: ' . $conn->error]);
    exit;
}

$stmt->bind_param("issss", $_POST['usuario_id'], $_POST['especialidad'], $rutaDestino, $_POST['ubicacion'], $_POST['telefono']);

if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error al guardar la solicitud: ' . $stmt->error]);
    exit;
}

$stmt->close();
$conn->close();

echo json_encode([
    'success' => true,
    'message' => 'Solicitud enviada correctamente. Espera la aprobación del administrador.'
]);
?>