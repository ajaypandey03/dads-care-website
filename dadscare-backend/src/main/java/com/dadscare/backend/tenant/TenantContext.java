package com.dadscare.backend.tenant;

/**
 * Per-request holder for the authenticated caller's tenant/user identity, populated by
 * {@link com.dadscare.backend.security.JwtAuthFilter} from the JWT — never from a
 * client-supplied header or request parameter.
 *
 * <p><b>Tenant isolation rule (binding, mirrors Velosyss's own convention):</b> every
 * repository query for a tenant-scoped entity must be explicitly scoped by
 * {@code organizationId}. This class does not install a global Hibernate filter — an
 * explicit {@code organizationId} parameter on every tenant-scoped repository method is
 * easier to audit in code review than an implicit filter that silently applies (or
 * silently fails to apply) depending on session state. Read {@code organizationId()} in
 * every service method that touches tenant data; never trust an id passed in the request
 * body/path without also checking it against this context.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> ORGANIZATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> PLATFORM_ADMIN = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long organizationId, Long userId, String role, boolean platformAdmin) {
        ORGANIZATION_ID.set(organizationId);
        USER_ID.set(userId);
        ROLE.set(role);
        PLATFORM_ADMIN.set(platformAdmin);
    }

    public static Long organizationId() {
        Long id = ORGANIZATION_ID.get();
        if (id == null) {
            throw new IllegalStateException(
                    "TenantContext.organizationId() called outside an authenticated request");
        }
        return id;
    }

    public static Long userId() {
        return USER_ID.get();
    }

    public static String role() {
        return ROLE.get();
    }

    /** True only for Dad's Care's own staff — see {@link com.dadscare.backend.user.User#isPlatformAdmin()}. */
    public static boolean isPlatformAdmin() {
        return Boolean.TRUE.equals(PLATFORM_ADMIN.get());
    }

    public static void clear() {
        ORGANIZATION_ID.remove();
        USER_ID.remove();
        ROLE.remove();
        PLATFORM_ADMIN.remove();
    }
}
