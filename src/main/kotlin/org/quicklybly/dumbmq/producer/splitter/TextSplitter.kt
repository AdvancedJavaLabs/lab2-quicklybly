package org.quicklybly.dumbmq.producer.splitter

import java.io.BufferedReader

abstract class TextSplitter(
    protected val reader: BufferedReader,
) : Iterator<String>
