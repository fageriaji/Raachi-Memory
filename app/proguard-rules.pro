# Protobuf Lite resolves generated message fields by their source names at runtime.
-keepclassmembers,allowoptimization class com.raachi.memory.data.settings.** extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
