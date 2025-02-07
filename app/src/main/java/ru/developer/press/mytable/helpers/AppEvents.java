package ru.developer.press.mytable.helpers;

public class AppEvents {

    private static AppEvents appEvents;
    private SaveEvent saveEvent;
    private boolean isSaveStart;
    private String fileName = "";
    private boolean startOpenTable;

    private AppEvents() {

    }

    public static AppEvents get() {
        if (appEvents == null)
            appEvents = new AppEvents();
        return appEvents;
    }

    public static void destroy() {
        if (appEvents != null)
            appEvents = null;
    }

    public String getFileName() {
        return fileName;
    }

    public void tableFileSaved() {
        if (saveEvent != null)
            saveEvent.saved();
        destroy();
    }

    public boolean isSaveStart() {
        return isSaveStart;
    }

    public void setSaveStart(boolean saveStart) {
        isSaveStart = saveStart;
    }

    public void tableFileSaveStart(String openName) {
        this.fileName = openName;
        isSaveStart = true;
    }

    public void setSaveEventListener(SaveEvent saveEvent) {
        this.saveEvent = saveEvent;
    }

    public void startOpenTable() {
        startOpenTable = true;
    }

    public boolean isStartOpenTable() {
        return startOpenTable;
    }

    public void openTableFinish() {
        startOpenTable = false;
    }

    public interface SaveEvent {
        public void saved();
    }
}
