package io.github.chsbuffer.revancedxposed.spotify.misc.privacy

import io.github.chsbuffer.revancedxposed.findMethodDirect
import io.github.chsbuffer.revancedxposed.fingerprint
import java.lang.reflect.Modifier

val shareCopyUrlFingerprint = findMethodDirect {
    runCatching {
        fingerprint {
            returns("Ljava/lang/Object;")
            parameters("Ljava/lang/Object;")
            strings("clipboard", "Spotify Link")
            methodMatcher { name = "invokeSuspend" }
        }
    }.getOrElse {
        runCatching {
            fingerprint {
                returns("Ljava/lang/Object;")
                parameters("Ljava/lang/Object;")
                strings("clipboard", "createNewSession failed")
                methodMatcher { name = "apply" }
            }
        }.getOrElse {
            // 9.1.84+: method renamed again; match any clipboard copier
            // returning Object and taking a single Object param.
            findMethod {
                matcher {
                    returnType("java.lang.Object")
                    paramTypes("java.lang.Object")
                    addUsingString("clipboard")
                }
            }.single()
        }
    }
}

val formatAndroidShareSheetUrlFingerprint = findMethodDirect {
    runCatching {
        findMethod {
            matcher {
                returnType("java.lang.String")
                addUsingNumber('\n'.code)
                modifiers = Modifier.PUBLIC or Modifier.STATIC
                paramTypes(null, "java.lang.String")
            }
        }.single {
            // exclude
            // `(PlayerState, String) -> String` usingNumbers(1, 10); usingStrings("")
            !it.usingStrings.contains("")
        }
    }.getOrElse {
        // 9.1.84+: ShareData signature changed; match the '\n' joiner loosely.
        findMethod {
            matcher {
                returnType("java.lang.String")
                addUsingNumber('\n'.code)
            }
        }.single {
            !it.usingStrings.contains("") &&
                    it.paramTypes.size == 2 &&
                    it.paramTypes[1] == "java.lang.String"
        }
    }

}