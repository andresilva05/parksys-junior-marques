package parksys.observer;

import parksys.enums.StatusVaga;

public interface EstacionamentoObserver {
    void onVagaAlterada(String idVaga, StatusVaga novoStatus);
}