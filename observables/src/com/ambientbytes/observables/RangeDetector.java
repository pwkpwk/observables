package com.ambientbytes.observables;

final class RangeDetector {
	
	private Range currentRange;
	
	static final class Range {
		private int start;
		private int length;
		
		Range(int start) {
			this.start = start;
			this.length = 1;
		}
		
		int start() {
			return start;
		}
		
		public int length() {
			return length;
		}
		
		boolean addIndex(int index) {
			boolean added = index == start + length;
			
			if (added) {
				length++;
			} else if (index == start - 1) {
				start = index;
				length++;
				added = true;
			}
			
			return added;
		}
	}

	RangeDetector() {
		this.currentRange = null;
	}
	
	Range addIndex(int index) {
		Range range = null;
		
		if (null == currentRange) {
			currentRange = new Range(index);
		} else {
			if (!currentRange.addIndex(index)) {
				range = currentRange;
				currentRange = new Range(index);
			}
		}
		
		return range;
	}
	
	Range finish() {
		Range range = currentRange;
		currentRange = null;
		return range;
	}
}
