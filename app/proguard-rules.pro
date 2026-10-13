# O serviço de acessibilidade e as Activities são referenciados pelo manifesto (mantidos pelo AGP).
# Remove logs de depuração e informativos do release (RNF-20).
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
