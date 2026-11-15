import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private final SistemaGestionAlojamiento sga = new SistemaGestionAlojamiento();
    private final ListView<Apartamento> listApartamentos = new ListView<>();
    private final ListView<String> listReservas = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                crearTabReservas(),
                crearTabOperacion(),
                crearTabApartamentos()
        );

        actualizarListas();

        Scene scene = new Scene(tabPane, 920, 620);
        primaryStage.setTitle("SGA - Aparta-Suites UQ (Salento, Quindío)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab crearTabReservas() {
        Tab tab = new Tab("1. Cotizar y Reservar");
        tab.setClosable(false);

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));

        ComboBox<Apartamento> cbApartamentos = new ComboBox<>(FXCollections.observableArrayList(sga.getApartamentos()));
        cbApartamentos.getSelectionModel().selectFirst();

        DatePicker dpEntrada = new DatePicker(LocalDate.now());
        DatePicker dpSalida = new DatePicker(LocalDate.now().plusDays(2));

        ComboBox<CanalReserva> cbCanal = new ComboBox<>(FXCollections.observableArrayList(CanalReserva.values()));
        cbCanal.getSelectionModel().selectFirst();

        TextField txtCodigoExterno = new TextField();
        txtCodigoExterno.setPromptText("Código externo (solo canal EXTERNA)");

        // Ocupantes dinámicos
        List<Ocupante> ocupantesTemp = new ArrayList<>();
        TextField txtNombreOcupante = new TextField();
        txtNombreOcupante.setPromptText("Nombre del ocupante");
        DatePicker dpNacOcupante = new DatePicker(LocalDate.of(1995, 1, 1));
        Button btnAgregarOcupante = new Button("Agregar Ocupante");
        Label lblOcupantes = new Label("Ocupantes agregados: 0");

        btnAgregarOcupante.setOnAction(e -> {
            if (!txtNombreOcupante.getText().isBlank() && dpNacOcupante.getValue() != null) {
                ocupantesTemp.add(new Ocupante(txtNombreOcupante.getText(), dpNacOcupante.getValue()));
                lblOcupantes.setText("Ocupantes agregados: " + ocupantesTemp.size() + " (Menores de 6 años no pagan)");
                txtNombreOcupante.clear();
            }
        });

        Label lblResultado = new Label("Cotización: $0 COP");
        lblResultado.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnCotizar = new Button("Cotizar Estancia");
        btnCotizar.setOnAction(e -> {
            try {
                long valor = sga.cotizarEstancia(cbApartamentos.getValue(), dpEntrada.getValue(), dpSalida.getValue(), ocupantesTemp);
                lblResultado.setText("Cotización Total: $" + valor + " COP");
            } catch (Exception ex) {
                mostrarAlerta("Error", ex.getMessage());
            }
        });

        Button btnReservar = new Button("Crear Reserva");
        btnReservar.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        btnReservar.setOnAction(e -> {
            try {
                Huesped titular = (Huesped) sga.getUsuarios().get(2); // Carlos Pérez de prueba
                Reserva r = sga.crearReserva(titular, cbApartamentos.getValue(), dpEntrada.getValue(),
                        dpSalida.getValue(), new ArrayList<>(ocupantesTemp), cbCanal.getValue(), txtCodigoExterno.getText());
                ocupantesTemp.clear();
                lblOcupantes.setText("Ocupantes agregados: 0");
                actualizarListas();
                mostrarAlerta("Reserva Creada", "ID: " + r.getIdReserva() + " | Estado: " + r.getEstado() + " | Valor: $" + r.getValorEstanciaCongelado());
            } catch (Exception ex) {
                mostrarAlerta("Validación de Negocio", ex.getMessage());
            }
        });

        HBox filaOcupantes = new HBox(10, txtNombreOcupante, dpNacOcupante, btnAgregarOcupante);
        HBox filaAcciones = new HBox(10, btnCotizar, btnReservar);

        layout.getChildren().addAll(
                new Label("Seleccione Apartamento:"), cbApartamentos,
                new HBox(10, new Label("Entrada:"), dpEntrada, new Label("Salida:"), dpSalida),
                new HBox(10, new Label("Canal:"), cbCanal, txtCodigoExterno),
                new Separator(),
                new Label("Registro de Ocupantes (La edad se calcula a la fecha de entrada):"),
                filaOcupantes, lblOcupantes,
                new Separator(),
                lblResultado, filaAcciones
        );

        tab.setContent(layout);
        return tab;
    }

    private Tab crearTabOperacion() {
        Tab tab = new Tab("2. Recepción, Pagos y Check-In/Out");
        tab.setClosable(false);

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));

        TextField txtMontoPago = new TextField();
        txtMontoPago.setPromptText("Monto a abonar (COP)");
        ComboBox<MedioPago> cbMedio = new ComboBox<>(FXCollections.observableArrayList(MedioPago.values()));
        cbMedio.getSelectionModel().selectFirst();

        Button btnPagar = new Button("Registrar Pago / Anticipo");
        btnPagar.setOnAction(e -> {
            Reserva r = obtenerReservaSeleccionada();
            if (r == null) return;
            try {
                long monto = Long.parseLong(txtMontoPago.getText());
                sga.registrarPago(r, monto, cbMedio.getValue());
                txtMontoPago.clear();
                actualizarListas();
            } catch (Exception ex) {
                mostrarAlerta("Error en pago", ex.getMessage());
            }
        });

        Button btnCheckIn = new Button("Hacer Check-In");
        btnCheckIn.setOnAction(e -> {
            Reserva r = obtenerReservaSeleccionada();
            if (r == null) return;
            try {
                sga.hacerCheckIn(r);
                actualizarListas();
            } catch (Exception ex) {
                mostrarAlerta("Error Check-In", ex.getMessage());
            }
        });

        Button btnCheckOut = new Button("Hacer Check-Out");
        btnCheckOut.setOnAction(e -> {
            Reserva r = obtenerReservaSeleccionada();
            if (r == null) return;
            try {
                sga.hacerCheckOut(r);
                actualizarListas();
            } catch (Exception ex) {
                mostrarAlerta("Error Check-Out", ex.getMessage());
            }
        });

        Button btnCancelar = new Button("Cancelar Reserva");
        btnCancelar.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> {
            Reserva r = obtenerReservaSeleccionada();
            if (r == null) return;
            try {
                long dev = sga.cancelarReserva(r);
                actualizarListas();
                mostrarAlerta("Reserva Cancelada", "Devolución calculada según política: $" + dev + " COP");
            } catch (Exception ex) {
                mostrarAlerta("Error", ex.getMessage());
            }
        });

        HBox controlesPago = new HBox(10, txtMontoPago, cbMedio, btnPagar);
        HBox controlesEstado = new HBox(10, btnCheckIn, btnCheckOut, btnCancelar);

        layout.getChildren().addAll(
                new Label("Reservas Registradas (Seleccione una):"),
                listReservas,
                controlesPago,
                controlesEstado
        );

        tab.setContent(layout);
        return tab;
    }

    private Tab crearTabApartamentos() {
        Tab tab = new Tab("3. Estado Operativo Apartamentos");
        tab.setClosable(false);

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));

        ComboBox<EstadoApartamento> cbEstados = new ComboBox<>(FXCollections.observableArrayList(EstadoApartamento.values()));
        cbEstados.getSelectionModel().selectFirst();

        Button btnCambiarEstado = new Button("Cambiar Estado Operativo");
        btnCambiarEstado.setOnAction(e -> {
            Apartamento apt = listApartamentos.getSelectionModel().getSelectedItem();
            if (apt != null) {
                apt.setEstado(cbEstados.getValue());
                actualizarListas();
            }
        });

        layout.getChildren().addAll(
                new Label("Inventario de Apartamentos:"),
                listApartamentos,
                new HBox(10, cbEstados, btnCambiarEstado)
        );

        tab.setContent(layout);
        return tab;
    }

    private Reserva obtenerReservaSeleccionada() {
        int idx = listReservas.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= sga.getReservas().size()) {
            mostrarAlerta("Aviso", "Seleccione una reserva de la lista.");
            return null;
        }
        return sga.getReservas().get(idx);
    }

    private void actualizarListas() {
        listApartamentos.setItems(FXCollections.observableArrayList(sga.getApartamentos()));
        List<String> resumenReservas = new ArrayList<>();
        for (Reserva r : sga.getReservas()) {
            resumenReservas.add(String.format("%s | %s | %s a %s | Canal: %s | Estado: %s | Total: $%d | Pagado: $%d | Saldo: $%d",
                    r.getIdReserva(), r.getApartamento().getCodigo(), r.getFechaEntrada(), r.getFechaSalida(),
                    r.getCanal(), r.getEstado(), r.getValorEstanciaCongelado(),
                    r.getFolio().calcularTotalPagado(), r.getFolio().calcularSaldo()));
        }
        listReservas.setItems(FXCollections.observableArrayList(resumenReservas));
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
