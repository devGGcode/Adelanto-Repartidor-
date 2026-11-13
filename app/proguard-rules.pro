# Reglas de ProGuard/R8 para la build de release.
# La app no usa reflexión sobre clases propias, así que no hace falta nada especial.
-dontwarn org.jetbrains.annotations.**
