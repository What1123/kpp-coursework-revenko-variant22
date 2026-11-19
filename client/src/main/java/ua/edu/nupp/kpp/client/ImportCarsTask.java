package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.nio.file.Path;

public class ImportCarsTask extends Task<CarCsvImporter.Result> {

    private final Path file;
    private final HttpCarServiceClient api;

    public ImportCarsTask(Path file, HttpCarServiceClient api) {
        this.file = file;
        this.api = api;
    }

    @Override
    protected CarCsvImporter.Result call() {
        updateProgress(0, 1);
        updateMessage("Читання файла " + file.getFileName());
        return new CarCsvImporter(api).importFile(file, (processed, total) -> {
            if (total == 0) {
                updateMessage("Немає рядків для імпорту");
                return;
            }
            updateProgress(processed, total);
            updateMessage("Оброблено рядків: " + processed + " з " + total);
        });
    }
}
