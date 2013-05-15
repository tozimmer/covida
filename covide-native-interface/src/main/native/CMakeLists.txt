# Copyright (c) 2012, Markus Weber All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without modification, are
# permitted provided that the following conditions are met:
# 
# Redistributions of source code must retain the above copyright notice, this list of
# conditions and the following disclaimer. Redistributions in binary form must reproduce the
# above copyright notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
# 
# Neither the name of the author nor the names of its contributors may be used to endorse or
# promote products derived from this software without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
# OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
# COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
# TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

###############################################################################
###                       CoVida Native API BUILD SCRIPT                    ###
###############################################################################
cmake_minimum_required( VERSION 2.8)

#==============================================================================
# Project Settings
#==============================================================================
PROJECT(COVIDANATIVEINTERFACE)
set (VERSION_MAJOR 1)
set (VERSION_MINOR 0)

#==============================================================================
# Path Settings
#==============================================================================

IF(NOT COVIDA_PATH)
    SET(ENV_COVIDA_PATH $ENV{COVIDA_PATH})
    IF(ENV_COVIDA_PATH)
        SET(COVIDA_PATH $ENV{ENV_COVIDA_PATH})
    ELSE(ENV_COVIDA_PATH)
        SET(COVIDA_PATH ${CMAKE_HOME_DIRECTORY})
    ENDIF(ENV_COVIDA_PATH)
ENDIF(NOT COVIDA_PATH)


# Set Cmake module path
SET(CMAKE_MODULE_PATH "${COVIDA_PATH}/cmake")
#==============================================================================
# Compiler Flags
#==============================================================================

SET( ENV{EXTERNAL_WARNINGS_NOT_ERRORS} TRUE)
ADD_DEFINITIONS(/W3 /wd4996)
    
#==============================================================================
# External Libs
#==============================================================================
# Microsoft Java Native interface 
FIND_PACKAGE(JNI REQUIRED)
# OpenCV
SET(OpenCV_DIR "C:/opencv2.4.0/build")
FIND_PACKAGE(OpenCV REQUIRED)
MESSAGE(STATUS ${COVIDA_PATH})
# Boost
if(WIN32) 
	IF(NOT BOOST_ROOT)
		SET(BOOST_ROOT "${COVIDA_PATH}/3rdparty/boost/boost_1_48_0")
		SET(Boost_NO_SYSTEM_PATHS TRUE)
	ENDIF(NOT BOOST_ROOT)
endif(WIN32)

ADD_DEFINITIONS(-DBOOST_ALL_NO_LIB=1) #tells boost to avoid auto-link
SET(Boost_ADDITIONAL_VERSIONS 1.46.1 1.47 1.48.0 1.52.0)
SET(Boost_USE_STATIC_LIBS ON)
SET(Boost_USE_MULTITHREAD ON)
SET(BOOST_MIN_VERSION "1.46.0")
FIND_PACKAGE(Boost ${BOOST_MIN_VERSION} COMPONENTS filesystem date_time program_options system thread python REQUIRED)
FIND_PACKAGE(pythonlibs REQUIRED)

#==============================================================================
# Process sources
#==============================================================================
INCLUDE_DIRECTORIES("${PROJECT_SOURCE_DIR}/include" "${JNI_INCLUDE_DIRS}" "${OpenCV_INCLUDE_DIRS}" "${Boost_INCLUDE_DIRS}" "${PYTHON_INCLUDE_PATH}")
SET(COVIDA_SOURCES ${PROJECT_SOURCE_DIR}/src/dllmain.cpp
                     ${PROJECT_SOURCE_DIR}/src/stdafx.cpp
					 ${PROJECT_SOURCE_DIR}/src/CovidaNativeInterface.cpp)
SET(COVIDA_HEADERS ${PROJECT_SOURCE_DIR}/include/de_dfki_covida_NativeInterface.h
				  	 ${PROJECT_SOURCE_DIR}/include/de_dfki_covida_NativeInterface_ShutdownHook.h
					 ${PROJECT_SOURCE_DIR}/include/CovidaNativeInterface.h
					 ${PROJECT_SOURCE_DIR}/include/stdafx.h
					 ${PROJECT_SOURCE_DIR}/include/targetver.h)
					 
# =========================== Classifier Library =================================
SET(CLASSIFIER_OPTIONAL_LINK_LIBRARIES "")
SET(CLASSIFIER_SOURCES  src/svm.cpp
						src/LibSvmWrapper.cpp)
SET(CLASSIFIER_HEADERS 	${PROJECT_SOURCE_DIR}/include/svm.h
						${PROJECT_SOURCE_DIR}/include/LibSvmWrapper.h)

ADD_LIBRARY(classifier STATIC ${CLASSIFIER_SOURCES} ${CLASSIFIER_HEADERS})
TARGET_LINK_LIBRARIES(classifier ${CLASSIFIER_OPTIONAL_LINK_LIBRARIES} ${OpenCV_LIBS})
					 
SET(IMAGE_PROCESSING_SOURCES  ${PROJECT_SOURCE_DIR}/src/algorithm.cpp 
                              ${PROJECT_SOURCE_DIR}/src/imgprocess_function.cpp)
SET(IMAGE_PROCESSING_HEADERS  ${PROJECT_SOURCE_DIR}/include/algorithm.h
							  ${PROJECT_SOURCE_DIR}/include/Labeling.h)

ADD_LIBRARY(algorithm STATIC ${IMAGE_PROCESSING_SOURCES} ${IMAGE_PROCESSING_HEADERS})
ADD_LIBRARY(CovidaNativeInterface SHARED ${COVIDA_SOURCES} ${COVIDA_HEADERS})
TARGET_LINK_LIBRARIES(CovidaNativeInterface algorithm classifier ${JNI_LIBRARIES} ${OpenCV_LIBS}  ${Boost_SYSTEM_LIBRARY} ${Boost_FILESYSTEM_LIBRARY} ${PYTHON_LIBRARIES} )
ADD_LIBRARY(CovidaNativeInterfaceStatic STATIC ${COVIDA_SOURCES} ${COVIDA_HEADERS})
TARGET_LINK_LIBRARIES(CovidaNativeInterfaceStatic algorithm classifier ${JNI_LIBRARIES} ${OpenCV_LIBS}  ${Boost_SYSTEM_LIBRARY} ${Boost_FILESYSTEM_LIBRARY} ${PYTHON_LIBRARIES} )

ADD_EXECUTABLE(test ${PROJECT_SOURCE_DIR}/src/test.cpp)
TARGET_LINK_LIBRARIES(test algorithm classifier CovidaNativeInterfaceStatic ${OpenCV_LIBS} ${Boost_SYSTEM_LIBRARY} ${Boost_FILESYSTEM_LIBRARY} ${Boost_PROGRAM_OPTIONS_LIBRARY} ${PYTHON_LIBRARIES})
