/*
 * SonarQube Python Plugin
 * Copyright (C) 2011-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.python.ruff;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public class RuffJsonReportReader {

  private final Consumer<Issue> consumer;
  private final JSONParser jsonParser;

  public static class Issue {
    @Nullable
    String ruleKey;
    @Nullable
    String filePath;
    @Nullable
    String message;
    @Nullable
    Integer startLocationRow;
    @Nullable
    Integer startLocationCol;
    @Nullable
    Integer endLocationRow;
    @Nullable
    Integer endLocationCol;
  }

  private RuffJsonReportReader(Consumer<Issue> consumer) {
    this.consumer = consumer;
    this.jsonParser = new JSONParser();
  }

  static void read(InputStream in, Consumer<Issue> consumer) throws IOException, ParseException {
    new RuffJsonReportReader(consumer).read(in);
  }

  private void read(InputStream in) throws IOException, ParseException {
    JSONArray files = (JSONArray) jsonParser.parse(new InputStreamReader(in, UTF_8));
    if (files != null) {
      ((Stream<JSONObject>) files.stream()).forEach(this::onResult);
    }
  }

  private void onResult(JSONObject result) {
    RuffJsonReportReader.Issue issue = new RuffJsonReportReader.Issue();
    issue.ruleKey = (String) result.get("code");
    issue.filePath = (String) result.get("filename");
    issue.message = (String) result.get("message");
    JSONObject location = (JSONObject) result.get("location");
    issue.startLocationCol = toInteger(location.get("column"));
    issue.startLocationRow = toInteger(location.get("row"));
    JSONObject endLocation = (JSONObject) result.get("end_location");
    issue.endLocationCol = correctEndLocationCol(endLocation.get("column"), issue.startLocationCol);
    issue.endLocationRow = toInteger(endLocation.get("row"));
    consumer.accept(issue);
  }

  /*
    Ruff returns the col number of the last char + 1.
    In order to properly read the col number we need to return the number of the last char.
   */
  private static Integer correctEndLocationCol(Object value, int startLocationCol) {
    Integer endLocationCol = toInteger(value);
    if (endLocationCol != null) {
      if (endLocationCol > startLocationCol) {
        return endLocationCol - 1;
      } else {
        return startLocationCol;
      }
    }
    return null;
  }

  private static Integer toInteger(Object value) {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return null;
  }

}
