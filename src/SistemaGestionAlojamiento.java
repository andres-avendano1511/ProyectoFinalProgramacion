import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SistemaGestionAlojamiento {
    private final List<Apartamento> apartamentos = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<String> logConflictosCanal = new ArrayList<>();
    private final Map<LocalDate, Temporada> calendarioTemporadas = new HashMap<>();

    public SistemaGestionAlojamiento() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        // Usuarios por defecto para pruebas rápidas
        usuarios.add(new Empleado("Admin UQ", "1001", LocalDate.of(1985, 5, 10), "admin@uq.edu.co", "1234", true));
        usuarios.add(new Empleado("Laura Recepción", "1002", LocalDate.of(1995, 8, 20), "recep@uq.edu.co", "1234", false));
        usuarios.add(new Huesped("Carlos Pérez", "1094888777", LocalDate.of(1990, 3, 15), "3101234567", "carlos@gmail.com", "1234"));

        // Apartamentos iniciales en Salento
        apartamentos.add(new Apartamento("APT-101", "Mirador del Cocora", 2, 4,
                List.of("Balcón", "Vista a las montañas"), 80000, 100000, 130000));
        apartamentos.add(new Apartamento("APT-102", "Suite Cafetera", 1, 2,
                List.of("Jacuzzi", "Chimenea"), 110000, 140000, 180000));

        // Configurar algunos días de temporada alta
        LocalDate hoy = LocalDate.now();
        calendarioTemporadas.put(hoy.plusDays(2), Temporada.MEDIA);
        calendarioTemporadas.put(hoy.plusDays(3), Temporada.ALTA);
    }

    public Temporada obtenerTemporadaFecha(LocalDate fecha) {
        return calendarioTemporadas.getOrDefault(fecha, Temporada.BAJA);
    }

    public long cotizarEstancia(Apartamento apt, LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes) {
        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
        }
        long personasQuePagan = ocupantes.stream().filter(o -> o.pagaTarifa(entrada)).count();
        long total = 0;

        for (LocalDate fecha = entrada; fecha.isBefore(salida); fecha = fecha.plusDays(1)) {
            Temporada temp = obtenerTemporadaFecha(fecha);
            total += apt.getTarifa(temp) * personasQuePagan;
        }
        return total;
    }

    public boolean estaDisponible(Apartamento apt, LocalDate entrada, LocalDate salida) {
        if (!apt.isActivo() || apt.getEstado() == EstadoApartamento.FUERA_SERVICIO) return false;
        for (Reserva r : reservas) {
            if (r.getApartamento().equals(apt) && r.esActiva() && r.comparteNocheCon(entrada, salida)) {
                return false;
            }
        }
        return true;
    }

    public Reserva crearReserva(Huesped titular, Apartamento apt, LocalDate entrada, LocalDate salida,
                                List<Ocupante> ocupantes, CanalReserva canal, String codigoExterno) {
        if (entrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten reservas con fecha anterior a hoy.");
        }
        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("Mínimo una noche de estancia.");
        }
        if (ocupantes.size() > apt.getCapacidadMaxima()) {
            throw new IllegalArgumentException("Se supera la capacidad máxima del apartamento (" + apt.getCapacidadMaxima() + ").");
        }
        if (canal == CanalReserva.EXTERNA && codigoExterno != null) {
            boolean duplicado = reservas.stream().anyMatch(r -> codigoExterno.equals(r.getCodigoExterno()));
            if (duplicado) throw new IllegalArgumentException("El código externo ya existe.");
        }
        if (!estaDisponible(apt, entrada, salida)) {
            if (canal == CanalReserva.EXTERNA) {
                logConflictosCanal.add("Conflicto canal externo [" + codigoExterno + "] en " + apt.getCodigo() + " (" + entrada + " a " + salida + ")");
            }
            throw new IllegalStateException("El apartamento no está disponible en las fechas seleccionadas (Doble venta evitada).");
        }

        long valorTotal = cotizarEstancia(apt, entrada, salida, ocupantes);
        String id = "RES-" + (reservas.size() + 1);
        Reserva nueva = new Reserva(id, codigoExterno, titular, apt, entrada, salida, ocupantes, canal, valorTotal);
        reservas.add(nueva);
        return nueva;
    }

    public void registrarPago(Reserva reserva, long monto, MedioPago medio) {
        reserva.getFolio().registrarPago("Abono de huésped", monto, medio);
        if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
            long anticipoRequerido = Math.round(reserva.getValorEstanciaCongelado() * 0.30);
            if (reserva.getCanal() != CanalReserva.PORTAL || reserva.getFolio().calcularTotalPagado() >= anticipoRequerido) {
                reserva.cambiarEstado(EstadoReserva.CONFIRMADA);
            }
        }
    }

    public void hacerCheckIn(Reserva reserva) {
        if (LocalDate.now().isBefore(reserva.getFechaEntrada())) {
            throw new IllegalStateException("Aún no es la fecha de entrada.");
        }
        if (reserva.getApartamento().getEstado() != EstadoApartamento.PREPARADO) {
            throw new IllegalStateException("El apartamento no está PREPARADO.");
        }
        if (!reserva.cambiarEstado(EstadoReserva.EN_CURSO)) {
            throw new IllegalStateException("La reserva debe estar CONFIRMADA para hacer check-in.");
        }
        reserva.getApartamento().setEstado(EstadoApartamento.OCUPADO);
    }

    public void hacerCheckOut(Reserva reserva) {
        if (!reserva.cambiarEstado(EstadoReserva.FINALIZADA)) {
            throw new IllegalStateException("Solo se puede hacer check-out a reservas EN_CURSO.");
        }
        reserva.getApartamento().setEstado(EstadoApartamento.PENDIENTE_PREPARACION);
    }

    public long calcularDevolucionCancelacion(Reserva reserva) {
        long pagado = reserva.getFolio().calcularTotalPagado();
        long diasAntelacion = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getFechaEntrada());

        if (diasAntelacion > 7) {
            return pagado; // Devuelve 100% de lo pagado
        } else if (diasAntelacion >= 2) {
            long retencion = Math.round(reserva.getValorEstanciaCongelado() * 0.50);
            return Math.max(0, pagado - retencion);
        } else {
            return 0L; // Retiene el 100%
        }
    }

    public long cancelarReserva(Reserva reserva) {
        long devolucion = calcularDevolucionCancelacion(reserva);
        if (!reserva.cambiarEstado(EstadoReserva.CANCELADA)) {
            throw new IllegalStateException("No se puede cancelar una reserva en estado " + reserva.getEstado());
        }
        if (devolucion > 0) {
            reserva.getFolio().agregarCargo("Devolución por cancelación", devolucion);
        }
        return devolucion;
    }

    public void verificarVencimiento24Horas() {
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.PENDIENTE && r.getFechaCreacion().isBefore(limite)) {
                r.cambiarEstado(EstadoReserva.CANCELADA);
            }
        }
    }

    public Usuario login(String correo, String password) {
        return usuarios.stream().filter(u -> u.autenticar(correo, password)).findFirst().orElse(null);
    }

    public List<Apartamento> getApartamentos() { return apartamentos; }
    public List<Reserva> getReservas() { return reservas; }
    public List<Usuario> getUsuarios() { return usuarios; }
}
