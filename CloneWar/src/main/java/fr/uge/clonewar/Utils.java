package fr.uge.clonewar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Utils {

  /**
   * Convert an Object to a JSON representation as a String.
   * @param object The object to serialize
   * @return The JSON
   * @throws JsonProcessingException if an error occur during the serialization
   */
  public static String toJson(Object object) throws JsonProcessingException {
    var mapper = new ObjectMapper();
    return mapper.writeValueAsString(object);
  }

  /**
   * Convert an Object to a JSON representation as a String with indentation.
   * @param object The object to serialize
   * @return The JSON
   * @throws JsonProcessingException if an error occur during the serialization
   */
  public static String toJsonIndented(Object object) throws JsonProcessingException {
    var mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper.writeValueAsString(object);
  }
}
