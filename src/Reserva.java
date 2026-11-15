import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Reserva implements GestionableEstado {
    private final String idReserva;
    private final String codigoExterno;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final List<Ocupante> ocupantes;
    private final CanalReserva canal;
    private final long valorEstanciaCongelado;
    private final long comisionPlataforma;
    private final LocalDateTime fechaCreacion;
    private EstadoReserva estado;
    private final Folio folio;

    public Reserva(String idReserva, String codigoExterno, Huesped titular, Apartamento apartamento,
                   LocalDate fechaEntrada, LocalDate fechaSalida, List<Ocupante> ocupantes,
                   CanalReserva canal, long valorEstanciaCongelado) {
        this.idReserva = idReserva;
        this.codigoExterno = codigoExterno;
        this.titular = titular;
        this.apartamento = apartamento;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.ocupantes = ocupantes;
        this.canal = canal;
        this.valorEstanciaCongelado = valorEstanciaCongelado;
        this.comisionPlataforma = (canal == CanalReserva.EXTERNA) ? Math.round(valorEstanciaCongelado * 0.15) : 0L;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoReserva.PENDIENTE;
        this.folio = new Folio();
        this.folio.agregarCargo("Alojamiento (" + fechaEntrada + " a " + fechaSalida + ")", valorEstanciaCongelado);
    }

    @Override
    public boolean cambiarEstado(EstadoReserva nuevoEstado) {
        boolean valido = switch (this.estado) {
            case PENDIENTE -> (nuevoEstado == EstadoReserva.CONFIRMADA || nuevoEstado == EstadoReserva.CANCELADA);
            case CONFIRMADA -> (nuevoEstado == EstadoReserva.EN_CURSO || nuevoEstado == EstadoReserva.CANCELADA || nuevoEstado == EstadoReserva.NO_SHOW);
            case EN_CURSO -> (nuevoEstado == EstadoReserva.FINALIZADA);
            case FINALIZADA, CANCELADA, NO_SHOW -> false;
        };
        if (valido) {
            this.estado = nuevoEstado;
        }
        return valido;
    }

    @Override
    public boolean esActiva() {
        return estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EN_CURSO;
    }

    public boolean comparteNocheCon(LocalDate otraEntrada, LocalDate otraSalida) {
        // La fecha de salida no ocupa noche: [entrada, salida)
        return this.fechaEntrada.isBefore(otraSalida) && otraEntrada.isBefore(this.fechaSalida);
    }

    public String getIdReserva() { return idReserva; }
    public String getCodigoExterno() { return codigoExterno; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamento() { return apartamento; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public List<Ocupante> getOcupantes() { return ocupantes; }
    public CanalReserva getCanal() { return canal; }
    public long getValorEstanciaCongelado() { return valorEstanciaCongelado; }
    public long getComisionPlataforma() { return comisionPlataforma; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public EstadoReserva getEstado() { return estado; }
    public Folio getFolio() { return folio; }
}