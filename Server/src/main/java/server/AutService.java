package server;

public interface AutService {
    /**
     * Получение ник по логину и паролю
     * Если учетка есть - вернет ее.
     * Если есть
     * @return никнейм
     * Если нет, null
     * */
    String getNicknameByLoginAndPassword (String login, String password);

    /**
     * Попытка регистрации новой учетной записи*/
    boolean registration (String login, String password, String nickname);
}
