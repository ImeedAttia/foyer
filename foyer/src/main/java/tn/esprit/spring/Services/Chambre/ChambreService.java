package tn.esprit.spring.Services.Chambre;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.spring.DAO.Entities.Bloc;
import tn.esprit.spring.DAO.Entities.Chambre;
import tn.esprit.spring.DAO.Entities.Reservation;
import tn.esprit.spring.DAO.Entities.TypeChambre;
import tn.esprit.spring.DAO.Repositories.BlocRepository;
import tn.esprit.spring.DAO.Repositories.ChambreRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ChambreService implements IChambreService {
    private final ChambreRepository chambreRepository;
    ChambreRepository repo;
    BlocRepository blocRepository;

    @Override
    public Chambre addOrUpdate(Chambre c) {
        return repo.save(c);
    }

    @Override
    public List<Chambre> findAll() {
        return repo.findAll();
    }

    @Override
    public Chambre findById(long id) {
        return repo.findById(id).orElse(new Chambre());
    }

    @Override
    public void deleteById(long id) {
        repo.deleteById(id);
    }

    @Override
    public void delete(Chambre c) {
        repo.delete(c);
    }

    @Override
    public List<Chambre> getChambresParNomBloc(String nomBloc) {
        return repo.findByBlocNomBloc(nomBloc);
    }

    @Override
    public long nbChambreParTypeEtBloc(TypeChambre type, long idBloc) {
        long compteur = 0;
        List<Chambre> list = chambreRepository.findAll();
        for (Chambre chambre : list) {
            if (chambre.getBloc().getIdBloc() == idBloc
                    && chambre.getTypeC().equals(type)) {
                compteur++;
            }
        }
        return compteur;
        //return repo.countByTypeCAndBlocIdBloc(type, idBloc);
    }

    @Override
    public List<Chambre> getChambresNonReserveParNomFoyerEtTypeChambre(String nomFoyer, TypeChambre type) {

        // Afficher les chambres non réservée, par typeChambre,
        // appartenant à un foyer donné par son nom, effectué durant
        // l’année universitaire actuelle.

        // Début "récuperer l'année universitaire actuelle"
        LocalDate dateDebutAU;
        LocalDate dateFinAU;
        int numReservation;
        int year = LocalDate.now().getYear() % 100;
        if (LocalDate.now().getMonthValue() <= 7) {
            dateDebutAU = LocalDate.of(Integer.parseInt("20" + (year - 1)), 9, 15);
            dateFinAU = LocalDate.of(Integer.parseInt("20" + year), 6, 30);
        } else {
            dateDebutAU = LocalDate.of(Integer.parseInt("20" + year), 9, 15);
            dateFinAU = LocalDate.of(Integer.parseInt("20" + (year + 1)), 6, 30);
        }
        // Fin "récuperer l'année universitaire actuelle"
        List<Chambre> listChambreDispo = new ArrayList<>();
        for (Chambre c : repo.findAll()) {
            if (c.getTypeC().equals(type) && c.getBloc().getFoyer().getNomFoyer().equals(nomFoyer)) { // Les chambres du foyer X et qui ont le type Y
                numReservation = 0;
                // nchoufou les réservations mta3 AU hethy binesba lil bit heki
                for (Reservation reservation : c.getReservations()) {
                    if (reservation.getAnneeUniversitaire().isBefore(dateFinAU) && reservation.getAnneeUniversitaire().isAfter(dateDebutAU)) {
                        numReservation++;
                    }
                }
                // nvérifi bil type w nombre des places elli l9ahom fer8in fi kol bit
                if (c.getTypeC().equals(TypeChambre.SIMPLE) && numReservation == 0) {
                    listChambreDispo.add(c);
                } else if (c.getTypeC().equals(TypeChambre.DOUBLE) && numReservation < 2) {
                    listChambreDispo.add(c);
                } else if (c.getTypeC().equals(TypeChambre.TRIPLE) && numReservation < 3) {
                    listChambreDispo.add(c);
                }
            }
        }
        return listChambreDispo;
    }

    @Scheduled(cron = "0 * * * * *")
    public void listeChambresParBloc() {
        for (Bloc b : blocRepository.findAll()) {
            log.info("Bloc => " + b.getNomBloc() +
                    " ayant une capacité " + b.getCapaciteBloc());
            if (!b.getChambres().isEmpty()) {
                log.info("La liste des chambres pour ce bloc: ");
                for (Chambre c : b.getChambres()) {
                    log.info("NumChambre: " + c.getNumeroChambre() +
                            " type: " + c.getTypeC());
                }
            } else {
                log.info("Pas de chambre disponible dans ce bloc");
            }
            log.info("********************");
        }
    }
}
