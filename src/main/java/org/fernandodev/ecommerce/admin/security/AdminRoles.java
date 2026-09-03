package org.fernandodev.ecommerce.admin.security;

/**
 * Nombres de rol y grupo administrativos exigidos en los endpoints de
 * administración.
 * <p>
 * Convención: deben existir en el IAM y estar asignados a los usuarios admin
 * (el rol se otorga vía la pertenencia al grupo).
 * </p>
 */
public final class AdminRoles {

    public static final String ADMIN_ROLE = "ADMINISTRADOR";
    public static final String ADMIN_GROUP = "ADMINISTRADORES";

    private AdminRoles() {
    }
}