import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Folio {
    private final List<MovimientoFolio> movimientos = new ArrayList<>();

    public void agregarCargo(String concepto, long valor) {
        if (valor <= 0) throw new IllegalArgumentException("El cargo debe ser positivo.");
        movimientos.add(new MovimientoFolio(concepto, valor, null));
    }

    public void registrarPago(String concepto, long valor, MedioPago medio) {
        if (valor <= 0) throw new IllegalArgumentException("El pago debe ser positivo.");
        movimientos.add(new MovimientoFolio(concepto, -valor, medio));
    }

    public long calcularSaldo() {
        return movimientos.stream().mapToLong(MovimientoFolio::getMonto).sum();
    }

    public long calcularTotalPagado() {
        return movimientos.stream()
                .filter(m -> m.getMonto() < 0)
                .mapToLong(m -> Math.abs(m.getMonto()))
                .sum();
    }

    // Regla: movimientos inalterables, solo se retorna lista de solo lectura
    public List<MovimientoFolio> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}