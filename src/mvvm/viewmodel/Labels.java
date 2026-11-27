package mvvm.viewmodel;

import mvvm.model.common.Gender;
import mvvm.model.common.LifeStatus;

import java.util.List;
import java.util.Optional;

/** Textos en español compartidos por los formateadores. */
final class Labels {

    static final String NO_DATA = "Sin datos";
    static final String NONE = "—";

    private Labels() {
    }

    static String status(LifeStatus status) {
        return switch (status) {
            case ALIVE -> "Vivo";
            case DEAD -> "Muerto";
            case UNKNOWN -> "Desconocido";
        };
    }

    static StatusTone tone(LifeStatus status) {
        return switch (status) {
            case ALIVE -> StatusTone.POSITIVE;
            case DEAD -> StatusTone.NEGATIVE;
            case UNKNOWN -> StatusTone.NEUTRAL;
        };
    }

    static String gender(Gender gender) {
        return switch (gender) {
            case MALE -> "Masculino";
            case FEMALE -> "Femenino";
            case GENDERLESS -> "Sin género";
            case AMBIGUOUS -> "Ambiguo";
            case UNKNOWN -> "Género desconocido";
        };
    }

    static String orNoData(Optional<String> value) {
        return value.orElse(NO_DATA);
    }

    static String join(List<String> values) {
        return values.isEmpty() ? NONE : String.join(", ", values);
    }

    /** {@code [1,2,3,5]} → {@code "4 episodios (1–3, 5)"}. */
    static String episodes(List<Integer> ids) {
        if (ids.isEmpty()) {
            return NONE;
        }
        List<Integer> sorted = ids.stream().distinct().sorted().toList();
        StringBuilder ranges = new StringBuilder();
        int start = sorted.getFirst();
        int previous = start;
        for (int i = 1; i <= sorted.size(); i++) {
            boolean last = i == sorted.size();
            if (!last && sorted.get(i) == previous + 1) {
                previous = sorted.get(i);
                continue;
            }
            if (!ranges.isEmpty()) {
                ranges.append(", ");
            }
            ranges.append(start == previous ? String.valueOf(start) : start + "–" + previous);
            if (!last) {
                start = sorted.get(i);
                previous = start;
            }
        }
        int count = sorted.size();
        return count + (count == 1 ? " episodio" : " episodios") + " (" + ranges + ")";
    }
}
