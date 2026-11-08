package es.upm.miw.practicaservicios.domain.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import es.upm.miw.practicaservicios.data.ConsultaUsgs;

public class ConsultaRepository {

    private final CollectionReference colllection;

    public ConsultaRepository(FirebaseUser user) {
        colllection = FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("consultas");
    }

    public Task<DocumentReference> save(ConsultaUsgs consulta) {
        return colllection.add(consulta);
    }

    public Task<QuerySnapshot> last(int limite) {
        return colllection.orderBy("info.fechaConsulta", Query.Direction.DESCENDING)
                .limit(limite)
                .get();
    }
}
