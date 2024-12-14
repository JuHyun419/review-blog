package com.jh.blog.batch.config

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableEncryptableProperties
class JasyptEncryptionConfig {

    @Bean("jasyptConfigEncryptor")
    fun jasyptConfigEncryptor(): StringEncryptor? {
        val passwordKey = System.getProperty("jasypt.encryptor.password") ?: throw RuntimeException()
        val encryptor = PooledPBEStringEncryptor()
        val config = SimpleStringPBEConfig().apply {
            password = passwordKey
            algorithm = "PBEWithMD5AndDES"
            setKeyObtentionIterations("1000")
            setPoolSize("1")
            setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator")
        }
        encryptor.setConfig(config)
        return encryptor
    }
}
