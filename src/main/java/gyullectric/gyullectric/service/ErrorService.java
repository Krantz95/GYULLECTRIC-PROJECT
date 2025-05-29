package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.ErrorReport;
import gyullectric.gyullectric.repository.ErrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ErrorService {
    private final ErrorRepository errorRepository;
    public ErrorReport errorSave(ErrorReport errorReport){
        errorRepository.save(errorReport);
        return errorReport;
    }
    public List<ErrorReport> allFindErrorList(){
        return errorRepository.findAll();
    }
    public Optional<ErrorReport> oneFindError(Long id){
        return errorRepository.findById(id);
    }

    public void deletError(Long id){
        errorRepository.deleteById(id);
    }
}
