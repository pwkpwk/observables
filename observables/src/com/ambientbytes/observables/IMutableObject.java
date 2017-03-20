package com.ambientbytes.observables;

/**
 * Interface implemented by items of observable collections that report their mutations,
 * so the collections may update themselves when items change. Examples of such collections
 * are sorted and filtering lists.
 * @author Pavel Karpenko
 *
 */
public interface IMutableObject {
	void addObserver(IObjectMutationObserver observer);
	void removeObserver(IObjectMutationObserver observer);
}
