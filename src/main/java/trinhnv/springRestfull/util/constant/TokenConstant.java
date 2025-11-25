package trinhnv.springRestfull.util.constant;

/**
 * ===================================================================
 * TOKEN CONSTANTS (STATELESS)
 * ===================================================================
 * 
 * Các hằng số liên quan đến JWT tokens.
 * 
 * NOTE: Các giá trị có thể config (expiration times)
 * được đặt trong application.properties và đọc qua TokenConfig.
 * 
 * @see trinhnv.springRestfull.config.TokenConfig
 * @author trinhnv
 */
public final class TokenConstant {

    /**
     * Token type for Authorization header
     */
    public static final String TOKEN_TYPE = "Bearer";

    /**
     * Token claims
     */
    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_TYPE_ACCESS = "access";
    public static final String CLAIM_TYPE_REFRESH = "refresh";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_AUTHORITIES = "authorities";

    private TokenConstant() {
        // Private constructor to prevent instantiation
    }
}
