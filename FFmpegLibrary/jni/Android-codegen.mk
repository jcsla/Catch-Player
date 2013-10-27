#echoprint-jni library
include $(CLEAR_VARS)

LOCAL_CPPFLAGS += -fexceptions

LOCAL_MODULE    :=echoprint-jni

LOCAL_SRC_FILES :=AndroidCodegen.cpp \
$(LOCAL_PATH)/codegen/src/Codegen.cpp \
$(LOCAL_PATH)/codegen/src/Whitening.cpp \
$(LOCAL_PATH)/codegen/src/SubbandAnalysis.cpp \
$(LOCAL_PATH)/codegen/src/MatrixUtility.cpp \
$(LOCAL_PATH)/codegen/src/Fingerprint.cpp \
$(LOCAL_PATH)/codegen/src/Base64.cpp \
$(LOCAL_PATH)/codegen/src/AudioStreamInput.cpp \
$(LOCAL_PATH)/codegen/src/AudioBufferInput.cpp

LOCAL_LDLIBS    :=-llog\
        -lz
LOCAL_C_INCLUDES :=$(LOCAL_PATH)/codegen/src \
            $(LOCAL_PATH)/boost_1_54_0               
 
include $(BUILD_SHARED_LIBRARY)