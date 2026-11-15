import java.time.LocalDateTime;

public class MovimientoFolio {
    private final String concepto;
    private final long monto; // Positivo = cargo, Negativo = pago o reverso
    private final MedioPago medioPago; // null si es cargo
    private final LocalDateTime fechaHora;

    public MovimientoFolio(String concepto, long monto, MedioPago medioPago) {
        this.concepto = concepto;
        this.monto = monto;
        this.medioPago = medioPago;
        this.fechaHora = LocalDateTime.now();
    }

    public String getConcepto() { return concepto; }
    public long getMonto() { return monto; }
    public MedioPago getMedioPago() { return medioPago; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
