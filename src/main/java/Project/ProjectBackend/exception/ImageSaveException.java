package Project.ProjectBackend.exception;

public class ImageSaveException extends RuntimeException {


    public ImageSaveException(String message) {
        super(message);
    }

    /**
     * 예외 메시지와 원인을 함께 전달하는 생성자
     *
     * @param message 예외 메시지
     * @param cause   원인 예외
     */
    public ImageSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
