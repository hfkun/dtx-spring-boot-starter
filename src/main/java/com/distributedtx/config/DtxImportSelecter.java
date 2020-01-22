package com.distributedtx.config;

import com.distributedtx.annotation.EnableDtxProvider;
import com.distributedtx.utils.ApplicationContextUtil;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.StringUtils;

public class DtxImportSelecter implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        String path = (String) annotationMetadata.getAnnotationAttributes(EnableDtxProvider.class.getName()).get("dtxTransactionalScanPath");
        if(StringUtils.isEmpty(path)){
            path = ((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getName();
            path = path.substring(0, path.lastIndexOf("."));
        }
        ApplicationContextUtil.dtxTransactionalScanPath = path;
        return new String[]{DtxProviderAutoConfiguration.class.getName()};
    }
}
