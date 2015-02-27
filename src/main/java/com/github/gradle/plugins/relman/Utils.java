package com.github.gradle.plugins.relman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.gradle.api.Project;

public class Utils {

  public static Project getRoot(Project project) {
    Project walkToRoot = project;
    while (walkToRoot != walkToRoot.getRootProject()) {
      walkToRoot = walkToRoot.getRootProject();
    }
    return walkToRoot;
  }

  public static <T extends Comparable<? super T>> List<T> sort(Collection<T> toSort){
	  List<T> retVal = new ArrayList<T>(toSort);
	  Collections.sort(retVal);
	  return retVal;
  }
}
