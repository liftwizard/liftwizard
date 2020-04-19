package com.liftwizard.junit.rule.match.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.json.JSONException;
import org.junit.rules.ErrorCollector;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JsonMatchRule extends ErrorCollector
{
    public void assertFileContents(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString,
            @Nonnull Class<?> callingClass)
    {
        try
        {
            JsonMatchRule.assertFileContentsOrThrow(resourceClassPathLocation, actualString, callingClass);
        }
        catch (@Nonnull URISyntaxException | FileNotFoundException | JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void assertFileContentsOrThrow(
            @Nonnull String resourceClassPathLocation,
            @Nonnull String actualString,
            @Nonnull Class<?> callingClass)
            throws URISyntaxException, FileNotFoundException, JSONException
    {
        InputStream inputStream = callingClass.getResourceAsStream(resourceClassPathLocation);
        if (inputStream == null)
        {
            String               packageName      = callingClass.getPackage().getName();
            ListIterable<String> packageNameParts = ArrayAdapter.adapt(packageName.split("\\."));
            Path                 testResources    = Paths.get("", "src", "test", "resources").toAbsolutePath();
            Path                 packagePath      = packageNameParts.injectInto(testResources, Path::resolve);
            File                 resourceFile     = packagePath.resolve(resourceClassPathLocation).toFile();

            assertThat(resourceFile.exists(), is(false));
            JsonMatchRule.writeStringToFile(actualString, resourceFile);
            fail(resourceClassPathLocation);
        }

        String expectedStringFromFile = JsonMatchRule.slurp(inputStream);
        URI    uri                    = callingClass.getResource(resourceClassPathLocation).toURI();
        if (!actualString.equals(expectedStringFromFile))
        {
            File file = new File(uri);
            JsonMatchRule.writeStringToFile(actualString, file);
        }
        JSONAssert.assertEquals(actualString, expectedStringFromFile, actualString, JSONCompareMode.STRICT);
    }

    private static String slurp(@Nonnull InputStream inputStream)
    {
        try (Scanner scanner = new Scanner(inputStream))
        {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private static void writeStringToFile(@Nonnull String string, @Nonnull File file) throws FileNotFoundException
    {
        try (PrintWriter printWriter = new PrintWriter(file))
        {
            printWriter.write(string);
        }
    }
}
